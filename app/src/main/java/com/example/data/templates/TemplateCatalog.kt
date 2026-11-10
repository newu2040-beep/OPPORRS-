package com.example.data.templates

import com.example.data.model.DocumentSection
import com.example.data.model.SectionType
import com.example.data.model.TextAlignment

enum class TemplateLayoutStyle {
    EDITORIAL_SERIF,
    MODERN_SIDEBAR,
    CLASSIC_CLEAN,
    EXECUTIVE_GRID,
    MINIMAL_MONO
}

data class DocumentTemplate(
    val id: String,
    val name: String,
    val category: String, // Career, Business, Education, Letters, Reports, Personal, Forms, Productivity, Creative
    val description: String,
    val accentHex: String,
    val fontFamily: String,
    val layoutStyle: TemplateLayoutStyle,
    val paperColorHex: String = "#FAF8F2",
    val isFeatured: Boolean = false,
    val isPopular: Boolean = false,
    val isCustom: Boolean = false,
    val defaultPageFormat: String = "A4",
    val defaultOrientation: String = "Portrait",
    val previewLines: List<String> = emptyList(),
    val buildSections: () -> List<DocumentSection>
)

object TemplateCatalog {
    val categories = listOf(
        "All",
        "Career",
        "Business",
        "Education",
        "Letters",
        "Reports",
        "Personal",
        "Forms",
        "Productivity",
        "Creative"
    )

    private val builtInTemplates: List<DocumentTemplate> by lazy {
        buildCareerTemplates() +
            buildBusinessTemplates() +
            buildEducationTemplates() +
            buildLettersTemplates() +
            TemplateCatalogExtended.buildReportsTemplates() +
            TemplateCatalogExtended.buildPersonalTemplates() +
            TemplateCatalogExtended.buildFormsTemplates() +
            TemplateCatalogExtended.buildProductivityTemplates() +
            TemplateCatalogExtended.buildCreativeTemplates() +
            TemplateCatalogExtended.buildPastelAndDesignerTemplates()
    }

    private val customTemplates = mutableListOf<DocumentTemplate>()

    val allTemplates: List<DocumentTemplate>
        get() = customTemplates + builtInTemplates

    val featuredTemplates: List<DocumentTemplate>
        get() = allTemplates.filter { it.isFeatured }.take(6)

    fun registerCustomTemplate(template: DocumentTemplate) {
        customTemplates.removeAll { it.id == template.id }
        customTemplates.add(0, template)
    }

    private fun cvSections(
        roleTitle: String,
        summaryText: String,
        expList: List<String>,
        eduText: String,
        skillsPipe: String
    ): List<DocumentSection> = listOf(
        DocumentSection(
            type = SectionType.HEADING,
            title = "PROFESSIONAL RESUME",
            content = "Rahul Shah — $roleTitle\nrahul.shah@example.com | +977 9812345678 | Kathmandu, Nepal",
            fontSizeSp = 18,
            isBold = true,
            alignment = TextAlignment.LEFT
        ),
        DocumentSection(
            type = SectionType.PARAGRAPH,
            title = "Professional Summary",
            content = summaryText,
            fontSizeSp = 12
        ),
        DocumentSection(
            type = SectionType.BULLET_LIST,
            title = "WORK EXPERIENCE",
            content = expList.joinToString("\n"),
            fontSizeSp = 12
        ),
        DocumentSection(
            type = SectionType.PARAGRAPH,
            title = "EDUCATION",
            content = eduText,
            fontSizeSp = 12
        ),
        DocumentSection(
            type = SectionType.PARAGRAPH,
            title = "SKILLS",
            content = skillsPipe,
            fontSizeSp = 12,
            isBold = true
        ),
        DocumentSection(
            type = SectionType.SIGNATURE,
            title = "Authorized Signature",
            content = "Rahul Shah"
        )
    )

    private fun buildCareerTemplates(): List<DocumentTemplate> {
        val careerDefinitions = listOf(
            Triple("career_01", "Professional CV", "Editorial two-tier curriculum vitae with executive summary and milestones."),
            Triple("career_02", "Modern Resume", "Clean modern resume with structured skill tags and impact metrics."),
            Triple("career_03", "Classic Resume", "Timeless serif-headed resume ideal for law, finance, and consulting."),
            Triple("career_04", "Student CV", "Academic-focused CV highlighting coursework, honors, and leadership."),
            Triple("career_05", "Graduate Resume", "Entry-level professional layout emphasizing capstones and internships."),
            Triple("career_06", "Technical Resume", "Systems & software engineering resume with tech stack matrix."),
            Triple("career_07", "Hospitality Resume", "Service excellence resume tailored for hotel & guest operations."),
            Triple("career_08", "Barista Resume", "Specialty coffee & customer experience resume with shift availability."),
            Triple("career_09", "Retail Resume", "Merchandising, POS operations, and sales target achievement resume."),
            Triple("career_10", "Cover Letter", "Persuasive executive cover letter aligned with your CV header."),
            Triple("career_11", "Job Application", "Formal employment application record with availability and references."),
            Triple("career_12", "Internship Application", "Targeted university internship application letter and skills table."),
            Triple("career_13", "Product Manager CV", "Outcome-driven PM resume highlighting product launches and KPIs."),
            Triple("career_14", "UX/UI Designer CV", "Design systems and user research portfolio-linked resume."),
            Triple("career_15", "Data Scientist CV", "Machine learning, statistical modeling, and research publications CV.")
        )

        val accents = listOf("#285B4B", "#1F4E5B", "#3B4D3E", "#2C4A3E", "#4A5D4E")
        val styles = TemplateLayoutStyle.values()

        return careerDefinitions.mapIndexed { idx, (id, name, desc) ->
            DocumentTemplate(
                id = id,
                name = name,
                category = "Career",
                description = desc,
                accentHex = accents[idx % accents.size],
                fontFamily = if (idx % 2 == 0) "Playfair Display" else "Plus Jakarta Sans",
                layoutStyle = styles[idx % styles.size],
                isFeatured = idx == 0 || idx == 9,
                isPopular = idx < 5,
                previewLines = listOf(
                    name.uppercase(),
                    "Rahul Shah • Professional Summary",
                    "Work Experience & Key Achievements",
                    "Education & Core Competencies"
                ),
                buildSections = {
                    if (name.contains("Letter") || name.contains("Application")) {
                        listOf(
                            DocumentSection(
                                type = SectionType.HEADING,
                                title = name.uppercase(),
                                content = "Rahul Shah\nKathmandu, Nepal • rahul.shah@example.com • +977 9812345678",
                                fontSizeSp = 16,
                                isBold = true
                            ),
                            DocumentSection(
                                type = SectionType.PARAGRAPH,
                                title = "Recipient Details",
                                content = "Hiring Committee\nOrganization Name\nCity, Country"
                            ),
                            DocumentSection(
                                type = SectionType.PARAGRAPH,
                                title = "Opening Statement",
                                content = "Dear Hiring Manager,\n\nI am writing to express my strong interest in the open position at your organization. With a proven track record of delivering measurable outcomes, cross-functional collaboration, and rigorous attention to detail, I am eager to contribute to your team's objectives."
                            ),
                            DocumentSection(
                                type = SectionType.BULLET_LIST,
                                title = "Key Qualifications",
                                content = "Delivered 35% workflow efficiency improvement across core projects\nCollaborated with multidisciplinary stakeholders to ship on schedule\nMaintained 99.4% quality compliance across client deliverables"
                            ),
                            DocumentSection(
                                type = SectionType.PARAGRAPH,
                                title = "Closing & Availability",
                                content = "Thank you for considering my application. I welcome the opportunity to discuss how my background aligns with your current needs.\n\nSincerely,\nRahul Shah"
                            ),
                            DocumentSection(
                                type = SectionType.SIGNATURE,
                                title = "Applicant Signature",
                                content = "Rahul Shah"
                            )
                        )
                    } else {
                        cvSections(
                            roleTitle = name.replace(" CV", "").replace(" Resume", "") + " Specialist",
                            summaryText = "A clean, editable professional introduction tailored for $name. Dedicated to high-craft execution, analytical problem solving, and collaborative leadership.",
                            expList = listOf(
                                "Senior Specialist • Global Studio LLC (2024 – Present): Led key initiatives, mentored junior colleagues, and improved delivery velocity by 28%.",
                                "Associate Specialist • Horizon Group (2022 – 2024): Coordinated cross-team workflows, authored technical documentation, and streamlined client onboarding."
                            ),
                            eduText = "B.Sc. in Computer Science & Management\nTribhuvan University • Graduated with Distinction",
                            skillsPipe = "Communication | Organization | Technical Skills | Strategic Planning | Analytical Reporting"
                        )
                    }
                }
            )
        }
    }

    private fun buildBusinessTemplates(): List<DocumentTemplate> {
        val specs = listOf(
            Triple("biz_01", "Business Proposal", "Comprehensive project scope, timeline, deliverables, and pricing proposal."),
            Triple("biz_02", "Business Plan", "Executive strategy, market analysis, operational model, and 3-year projections."),
            Triple("biz_03", "Invoice", "Itemized commercial invoice with tax calculation, payment terms, and remittance."),
            Triple("biz_04", "Receipt", "Official payment acknowledgment receipt with transaction reference."),
            Triple("biz_05", "Quotation", "Formal price estimate with validity period and itemized service breakdown."),
            Triple("biz_06", "Purchase Order", "Vendor procurement order with SKU table, shipping instructions, and authorization."),
            Triple("biz_07", "Payment Reminder", "Courteous overdue invoice reminder with statement summary."),
            Triple("biz_08", "Company Profile", "Corporate overview showcasing mission, leadership, and flagship case studies."),
            Triple("biz_09", "Client Onboarding Form", "Structured kickoff intake form for new agency or consulting clients."),
            Triple("biz_10", "Service Agreement Template", "Master services contract outlining scope, IP ownership, and SLA terms."),
            Triple("biz_11", "Non-Disclosure Agreement", "Mutual confidentiality agreement protecting proprietary business information."),
            Triple("biz_12", "Statement of Work (SOW)", "Milestone-based deliverable schedule and acceptance criteria."),
            Triple("biz_13", "Partnership Proposal", "Strategic B2B alliance and co-marketing proposal."),
            Triple("biz_14", "Board Resolution", "Formal corporate board of directors resolution record."),
            Triple("biz_15", "Vendor Evaluation Matrix", "Comparative supplier scoring sheet across cost, quality, and compliance.")
        )

        return specs.mapIndexed { idx, (id, name, desc) ->
            DocumentTemplate(
                id = id,
                name = name,
                category = "Business",
                description = desc,
                accentHex = if (idx % 2 == 0) "#285B4B" else "#35584A",
                fontFamily = "Plus Jakarta Sans",
                layoutStyle = TemplateLayoutStyle.EXECUTIVE_GRID,
                isFeatured = idx == 0,
                isPopular = idx < 4,
                previewLines = listOf(
                    name.uppercase(),
                    "Prepared by: Rahul Shah • Ref #OP-${202600 + idx}",
                    "Executive Summary & Scope Table",
                    "Terms, Schedule & Authorization"
                ),
                buildSections = {
                    listOf(
                        DocumentSection(
                            type = SectionType.HEADING,
                            title = name.uppercase(),
                            content = "Reference: OP-${202600 + idx} • Date: November 2026\nPrepared by: Opporrs Advisory Studio",
                            fontSizeSp = 17,
                            isBold = true
                        ),
                        DocumentSection(
                            type = SectionType.PARAGRAPH,
                            title = "1. Executive Overview",
                            content = "$desc This document establishes the formal framework, deliverables, and commercial terms agreed between the participating parties."
                        ),
                        DocumentSection(
                            type = SectionType.TABLE,
                            title = "2. Itemized Breakdown & Schedule",
                            content = "Structured line items for $name",
                            tableRows = listOf(
                                listOf("Item / Deliverable", "Quantity / Timeline", "Unit Rate", "Total"),
                                listOf("Phase 1: Discovery & Architecture", "2 Weeks", "$1,200.00", "$2,400.00"),
                                listOf("Phase 2: Core Implementation", "4 Weeks", "$1,500.00", "$6,000.00"),
                                listOf("Phase 3: Quality Assurance & Handover", "1 Week", "$1,100.00", "$1,100.00")
                            )
                        ),
                        DocumentSection(
                            type = SectionType.BULLET_LIST,
                            title = "3. Terms & Conditions",
                            content = "All deliverables are subject to written sign-off at each milestone.\nPayment is due within 15 business days of invoice issuance.\nBoth parties agree to maintain strict confidentiality regarding proprietary data."
                        ),
                        DocumentSection(
                            type = SectionType.SIGNATURE,
                            title = "Authorized Signatory",
                            content = "Rahul Shah"
                        )
                    )
                }
            )
        }
    }

    private fun buildEducationTemplates(): List<DocumentTemplate> {
        val specs = listOf(
            Triple("edu_01", "Scholarship Application", "Merit and need-based academic scholarship statement with honors table."),
            Triple("edu_02", "Statement of Purpose", "Graduate school admissions essay detailing research interests and goals."),
            Triple("edu_03", "Study Plan", "Weekly academic study schedule and exam preparation roadmap."),
            Triple("edu_04", "Assignment Cover Page", "University coursework submission title sheet with student metadata."),
            Triple("edu_05", "Research Proposal", "Thesis or dissertation research question, methodology, and literature review."),
            Triple("edu_06", "Project Outline", "Capstone academic project architecture, milestones, and bibliography."),
            Triple("edu_07", "Academic Appeal", "Formal petition to academic standing or grade review committee."),
            Triple("edu_08", "Internship Request", "Faculty-endorsed academic credit internship request letter."),
            Triple("edu_09", "Lab Report Template", "Hypothesis, experimental apparatus, observations, and error analysis."),
            Triple("edu_10", "Course Syllabus", "Semester curriculum, grading rubric, and weekly reading schedule."),
            Triple("edu_11", "Literature Review Matrix", "Comparative synthesis table of peer-reviewed academic sources."),
            Triple("edu_12", "Thesis Abstract", "Concise 300-word academic abstract with keywords and advisor sign-off.")
        )

        return specs.mapIndexed { idx, (id, name, desc) ->
            DocumentTemplate(
                id = id,
                name = name,
                category = "Education",
                description = desc,
                accentHex = "#285B4B",
                fontFamily = "Playfair Display",
                layoutStyle = TemplateLayoutStyle.EDITORIAL_SERIF,
                isPopular = idx < 3,
                previewLines = listOf(
                    name.uppercase(),
                    "Academic Department & Institution",
                    "Abstract, Objectives & Methodology",
                    "References & Faculty Endorsement"
                ),
                buildSections = {
                    listOf(
                        DocumentSection(
                            type = SectionType.HEADING,
                            title = name.uppercase(),
                            content = "Candidate: Rahul Shah • Department of Computer Science\nAcademic Session: 2026–2027",
                            fontSizeSp = 17,
                            isBold = true
                        ),
                        DocumentSection(
                            type = SectionType.PARAGRAPH,
                            title = "1. Purpose & Academic Context",
                            content = "$desc My academic trajectory has consistently focused on bridging theoretical foundations with practical, human-centered systems."
                        ),
                        DocumentSection(
                            type = SectionType.NUMBERED_LIST,
                            title = "2. Core Objectives & Methodology",
                            content = "Formulate a rigorous analytical framework grounded in peer-reviewed literature.\nConduct empirical evaluation across representative benchmarks.\nSynthesize findings into actionable recommendations and open documentation."
                        ),
                        DocumentSection(
                            type = SectionType.TABLE,
                            title = "3. Academic Milestones & Evaluation",
                            content = "Planned schedule",
                            tableRows = listOf(
                                listOf("Milestone", "Target Date", "Deliverable", "Status"),
                                listOf("Literature Review", "Week 3", "Annotated Bibliography", "Completed"),
                                listOf("Methodology Draft", "Week 6", "Experimental Protocol", "In Progress"),
                                listOf("Final Submission", "Week 12", "Complete Manuscript", "Scheduled")
                            )
                        )
                    )
                }
            )
        }
    }

    private fun buildLettersTemplates(): List<DocumentTemplate> {
        val specs = listOf(
            Triple("let_01", "Resignation Letter", "Gracious notice of resignation with transition plan and final date."),
            Triple("let_02", "Leave Application", "Formal request for medical, personal, or annual leave of absence."),
            Triple("let_03", "Recommendation Request", "Polite request to a professor or manager for a reference letter."),
            Triple("let_04", "Complaint Letter", "Constructive consumer or service grievance letter requesting resolution."),
            Triple("let_05", "Formal Request", "Official administrative request for records, permissions, or resources."),
            Triple("let_06", "Thank-You Letter", "Post-interview or post-project appreciation note."),
            Triple("let_07", "Permission Letter", "Formal authorization request for event, facility, or copyright usage."),
            Triple("let_08", "Reference Letter", "Strong professional endorsement letter written for a colleague or student."),
            Triple("let_09", "Offer Acceptance Letter", "Formal confirmation of job offer acceptance and start date."),
            Triple("let_10", "Contract Termination Letter", "Official notice ending a service agreement in accordance with notice clauses."),
            Triple("let_11", "Apology & Resolution Letter", "Professional service recovery letter addressing a client concern."),
            Triple("let_12", "Visa Sponsorship Letter", "Employer confirmation of employment and travel purpose for consulate.")
        )

        return specs.map { (id, name, desc) ->
            DocumentTemplate(
                id = id,
                name = name,
                category = "Letters",
                description = desc,
                accentHex = "#285B4B",
                fontFamily = "Playfair Display",
                layoutStyle = TemplateLayoutStyle.CLASSIC_CLEAN,
                previewLines = listOf(
                    name.uppercase(),
                    "Date & Recipient Address Block",
                    "Formal Salutation & Core Message",
                    "Sign-off & Handwritten Signature"
                ),
                buildSections = {
                    listOf(
                        DocumentSection(
                            type = SectionType.HEADING,
                            title = name.uppercase(),
                            content = "Date: November 10, 2026\nFrom: Rahul Shah • Kathmandu, Nepal",
                            fontSizeSp = 16,
                            isBold = true
                        ),
                        DocumentSection(
                            type = SectionType.PARAGRAPH,
                            title = "Recipient",
                            content = "To: The Relevant Authority / Department Head\nSubject: Formal $name"
                        ),
                        DocumentSection(
                            type = SectionType.PARAGRAPH,
                            title = "Letter Body",
                            content = "Dear Sir or Madam,\n\nI am writing to formally submit this $name. $desc\n\nI have ensured that all prerequisites and supporting documentation are prepared to facilitate a smooth process. Please let me know if any additional information is required from my side."
                        ),
                        DocumentSection(
                            type = SectionType.PARAGRAPH,
                            title = "Valediction",
                            content = "Thank you for your time, consideration, and prompt attention to this matter.\n\nWarm regards,\nRahul Shah"
                        ),
                        DocumentSection(
                            type = SectionType.SIGNATURE,
                            title = "Sender Signature",
                            content = "Rahul Shah"
                        )
                    )
                }
            )
        }
    }
}
