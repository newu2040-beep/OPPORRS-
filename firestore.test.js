const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validDocPayload(userId, now) {
  return {
    userId,
    title: "Rahul Shah Resume",
    category: "Career",
    templateId: "career_cv_01",
    folderId: "",
    status: "draft",
    isFavorite: false,
    pageFormat: "A4",
    orientation: "Portrait",
    marginMm: 20,
    accentHex: "#285B4B",
    fontFamily: "Playfair Display",
    sectionsData: "[]",
    signatureSvg: "",
    scannedPagesData: "[]",
    wordCount: 120,
    pageCount: 1,
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated user cannot read or write documents", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("documents").doc("doc_1").get());
  await assertFails(
    unauthDb.collection("documents").doc("doc_1").set(validDocPayload(ALICE_UID, new Date()))
  );
});

test("Authenticated user can create, read, update, and query their own documents", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb.collection("documents").doc("doc_alice_1").set(validDocPayload(ALICE_UID, now))
  );
  await assertSucceeds(
    aliceDb.collection("documents").where("userId", "==", ALICE_UID).get()
  );
  await assertSucceeds(
    aliceDb.collection("documents").doc("doc_alice_1").update({
      title: "Updated Resume Title",
      status: "completed",
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user cannot read, update, or delete another user's document", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("documents").doc("doc_bob_1").set(validDocPayload(BOB_UID, now));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("documents").doc("doc_bob_1").get());
  await assertFails(
    aliceDb.collection("documents").doc("doc_bob_1").update({
      title: "Hacked Title",
      updatedAt: new Date(),
    })
  );
  await assertFails(aliceDb.collection("documents").doc("doc_bob_1").delete());
});

test("Shadow update with ghost field or invalid enum is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb.collection("documents").doc("doc_alice_2").set(validDocPayload(ALICE_UID, now))
  );
  // Ghost field rejection
  await assertFails(
    aliceDb.collection("documents").doc("doc_alice_2").update({
      title: "Valid Title",
      isVerifiedAdmin: true,
      updatedAt: new Date(),
    })
  );
  // Invalid status enum rejection
  await assertFails(
    aliceDb.collection("documents").doc("doc_alice_2").update({
      status: "super_status",
      updatedAt: new Date(),
    })
  );
});

test("User profile PII isolation prevents cross-user access", async () => {
  const now = new Date();
  const bobProfile = {
    userId: BOB_UID,
    fullName: "Bob Smith",
    jobTitle: "Architect",
    email: "bob@example.com",
    phone: "+123456789",
    location: "New York",
    website: "https://bob.example.com",
    summary: "Experienced architect",
    experienceText: "10 years at Studio",
    educationText: "B.Arch",
    skillsText: "CAD | Design",
    referencesText: "Available on request",
    avatarUri: "",
    defaultSignatureSvg: "",
    createdAt: now,
    updatedAt: now,
  };
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("profiles").doc(BOB_UID).set(bobProfile);
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("profiles").doc(BOB_UID).get());

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("profiles").doc(BOB_UID).get());
});
