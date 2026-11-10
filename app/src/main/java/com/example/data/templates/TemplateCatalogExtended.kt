package com.example.data.templates

import com.example.data.model.DocumentSection
import com.example.data.model.SectionType

object TemplateCatalogExtended {

    fun buildReportsTemplates(): List<DocumentTemplate> {
        val specs = listOf(
            Triple("rep_01", "Project Status Report", "RAG status indicator, milestone progress, risks, and next week's priorities."),
            Triple("rep_02", "Weekly Report", "Structured summary of weekly accomplishments, blockers, and KPI metrics."),
            Triple("rep_03", "Monthly Report", "Departmental performance review with month-over-month variance table."),
            Triple("rep_04", "Annual Report", "Comprehensive yearly review of achievements, financials, and strategic outlook."),
            Triple("rep_05", "Meeting Minutes", "Attendees, agenda decisions, discussion notes, and assigned action items."),
            Triple("rep_06", "Meeting Agenda", "Time-boxed meeting topics, presenters, and preparation materials."),
            Triple("rep_07", "Incident Report", "Root-cause analysis, timeline of events, impact, and corrective actions."),
            Triple("rep_08", "Inspection Checklist", "Facility, safety, or quality assurance audit report with pass/fail grid."),
            Triple("rep_09", "Executive Briefing", "One-page board briefing on strategic options and recommended decision."),
            Triple("rep_10", "Financial Variance Report", "Budget vs. actual expenditure analysis across cost centers."),
            Triple("rep_11", "Post-Mortem Retrospective", "Sprint or launch retrospective covering wins, challenges, and action items."),
            Triple("rep_12", "Field Visit Report", "On-site observation log, stakeholder interviews, and follow-up plan.")
        )

        return specs.map { (id, name, desc) ->
            DocumentTemplate(
                id = id,
                name = name,
                category = "Reports",
                description = desc,
                accentHex = "#285B4B",
                fontFamily = "Plus Jakarta Sans",
                layoutStyle = TemplateLayoutStyle.EXECUTIVE_GRID,
                previewLines = listOf(
                    name.uppercase(),
                    "Executive Summary & Key Metrics",
                    "Status Table & Action Items",
                    "Risks & Recommendations"
                ),
                buildSections = {
                    listOf(
                        DocumentSection(
                            type = SectionType.HEADING,
                            title = name.uppercase(),
                            content = "Prepared by: Rahul Shah • Reporting Period: Q4 2026",
                            fontSizeSp = 17,
                            isBold = true
                        ),
                        DocumentSection(
                            type = SectionType.PARAGRAPH,
                            title = "1. Executive Summary",
                            content = "$desc Overall progress remains on track with key performance targets met across primary workstreams."
                        ),
                        DocumentSection(
                            type = SectionType.TABLE,
                            title = "2. Key Performance & Action Tracker",
                            content = "Metrics and owners",
                            tableRows = listOf(
                                listOf("Workstream / Metric", "Owner", "Status", "Notes"),
                                listOf("Core Platform Delivery", "R. Shah", "On Track", "96% milestone completion"),
                                listOf("Quality & Security Audit", "A. Verma", "Completed", "Zero critical findings"),
                                listOf("Stakeholder Rollout", "M. Karki", "In Review", "Scheduled for Friday")
                            )
                        ),
                        DocumentSection(
                            type = SectionType.BULLET_LIST,
                            title = "3. Risks & Next Steps",
                            content = "Finalize resource allocation for upcoming sprint cycle\nComplete cross-team review of external dependencies\nPublish updated metrics dashboard to leadership"
                        )
                    )
                }
            )
        }
    }

    fun buildPersonalTemplates(): List<DocumentTemplate> {
        val specs = listOf(
            Triple("per_01", "Personal Information Sheet", "Consolidated personal, medical, and identification record."),
            Triple("per_02", "Emergency Contact Sheet", "Household and travel emergency contacts, doctors, and insurance numbers."),
            Triple("per_03", "Travel Itinerary", "Flight, hotel, local transport, and daily sightseeing schedule."),
            Triple("per_04", "Household Inventory", "Room-by-room asset register with serial numbers and estimated values."),
            Triple("per_05", "Personal Budget Worksheet", "Monthly income, fixed expenses, savings goals, and cashflow table."),
            Triple("per_06", "Goal Planner", "Quarterly personal and professional OKRs with habit tracking."),
            Triple("per_07", "Authorization Letter", "Proxy letter authorizing a representative to collect documents or act on your behalf."),
            Triple("per_08", "Medical History Summary", "Allergies, current medications, immunizations, and past procedures."),
            Triple("per_09", "Moving Checklist", "Address change notifications, utility transfers, and packing inventory."),
            Triple("per_10", "Vehicle Maintenance Log", "Service dates, mileage, parts replaced, and warranty notes."),
            Triple("per_11", "Event Planning Checklist", "Guest list summary, catering budget, and run-of-show timeline."),
            Triple("per_12", "Lease & Rental Agreement", "Residential tenancy terms, security deposit, and maintenance obligations.")
        )

        return specs.map { (id, name, desc) ->
            DocumentTemplate(
                id = id,
                name = name,
                category = "Personal",
                description = desc,
                accentHex = "#719080",
                fontFamily = "Plus Jakarta Sans",
                layoutStyle = TemplateLayoutStyle.CLASSIC_CLEAN,
                previewLines = listOf(
                    name.uppercase(),
                    "Personal Record & Details",
                    "Structured Schedule / Inventory Table",
                    "Notes & Verification"
                ),
                buildSections = {
                    listOf(
                        DocumentSection(
                            type = SectionType.HEADING,
                            title = name.uppercase(),
                            content = "Owner: Rahul Shah • Last Updated: November 2026",
                            fontSizeSp = 16,
                            isBold = true
                        ),
                        DocumentSection(
                            type = SectionType.PARAGRAPH,
                            title = "Overview",
                            content = desc
                        ),
                        DocumentSection(
                            type = SectionType.TABLE,
                            title = "Structured Entries",
                            content = "Key records for $name",
                            tableRows = listOf(
                                listOf("Category / Item", "Primary Detail", "Reference / Contact", "Notes"),
                                listOf("Primary Entry", "Verified Record", "+977 9812345678", "Active"),
                                listOf("Secondary Entry", "Backup Record", "Kathmandu, Nepal", "Confirmed"),
                                listOf("Contingency Item", "Reviewed Q4", "Ref #2026-A", "Complete")
                            )
                        ),
                        DocumentSection(
                            type = SectionType.SIGNATURE,
                            title = "Owner Signature",
                            content = "Rahul Shah"
                        )
                    )
                }
            )
        }
    }

    fun buildFormsTemplates(): List<DocumentTemplate> {
        val specs = listOf(
            Triple("frm_01", "Registration Form", "Participant enrollment form with contact details and consent."),
            Triple("frm_02", "Feedback Form", "Customer or attendee satisfaction survey with rating grid."),
            Triple("frm_03", "Application Form", "General membership or program application record."),
            Triple("frm_04", "Attendance Sheet", "Training session or workshop sign-in register."),
            Triple("frm_05", "Expense Claim", "Employee travel and business expense reimbursement voucher."),
            Triple("frm_06", "Customer Details Form", "KYC and client contact intake sheet."),
            Triple("frm_07", "Employee Information Form", "HR onboarding record with payroll, tax, and emergency contact fields."),
            Triple("frm_08", "Equipment Checkout Form", "Asset serial number, borrower acknowledgment, and return condition."),
            Triple("frm_09", "Visitor Log Sheet", "Facility security check-in and badge tracking table."),
            Triple("frm_10", "Leave Request Form", "HR time-off request with manager approval block."),
            Triple("frm_11", "Timesheet Log", "Weekly billable hours log across client projects."),
            Triple("frm_12", "Consent & Release Form", "Media, photography, or activity liability release form.")
        )

        return specs.map { (id, name, desc) ->
            DocumentTemplate(
                id = id,
                name = name,
                category = "Forms",
                description = desc,
                accentHex = "#285B4B",
                fontFamily = "Plus Jakarta Sans",
                layoutStyle = TemplateLayoutStyle.EXECUTIVE_GRID,
                previewLines = listOf(
                    name.uppercase(),
                    "Participant / Submitter Information",
                    "Structured Field Grid",
                    "Declaration & Signature"
                ),
                buildSections = {
                    listOf(
                        DocumentSection(
                            type = SectionType.HEADING,
                            title = name.uppercase(),
                            content = "Form ID: FRM-${id.uppercase()} • Date: November 2026",
                            fontSizeSp = 16,
                            isBold = true
                        ),
                        DocumentSection(
                            type = SectionType.PARAGRAPH,
                            title = "Instructions",
                            content = "$desc Please verify all entries before signing below."
                        ),
                        DocumentSection(
                            type = SectionType.TABLE,
                            title = "Form Fields & Entries",
                            content = "Tabular form data",
                            tableRows = listOf(
                                listOf("Field Name", "Entered Value", "Verification"),
                                listOf("Full Name", "Rahul Shah", "Verified"),
                                listOf("Department / Organization", "Product & Engineering", "Verified"),
                                listOf("Contact Email", "rahul.shah@example.com", "Verified"),
                                listOf("Submission Date", "2026-11-10", "Approved")
                            )
                        ),
                        DocumentSection(
                            type = SectionType.SIGNATURE,
                            title = "Authorized Signature",
                            content = "Rahul Shah"
                        )
                    )
                }
            )
        }
    }

    fun buildProductivityTemplates(): List<DocumentTemplate> {
        val specs = listOf(
            Triple("prd_01", "OKR Strategy Sheet", "Quarterly Objectives and Key Results with confidence scoring."),
            Triple("prd_02", "Sprint Planning Board", "Two-week agile sprint goals, velocity capacity, and committed stories."),
            Triple("prd_03", "Eisenhower Priority Matrix", "Urgent vs. important decision quadrant for executive focus."),
            Triple("prd_04", "Daily Timeblock Planner", "Deep-work hourly schedule, top 3 priorities, and evening review."),
            Triple("prd_05", "Project Raid Log", "Risks, Assumptions, Issues, and Dependencies tracker."),
            Triple("prd_06", "Product Requirements Doc (PRD)", "Problem statement, user stories, acceptance criteria, and telemetry."),
            Triple("prd_07", "Decision Log (DACI)", "Driver, Approver, Contributors, and Informed decision record."),
            Triple("prd_08", "Release Readiness Checklist", "Pre-launch QA, security, documentation, and rollback verification."),
            Triple("prd_09", "1-on-1 Coaching Notes", "Career growth, feedback, roadblocks, and follow-up commitments."),
            Triple("prd_10", "Habit & Routine Tracker", "30-day consistency matrix for key professional habits.")
        )

        return specs.map { (id, name, desc) ->
            DocumentTemplate(
                id = id,
                name = name,
                category = "Productivity",
                description = desc,
                accentHex = "#285B4B",
                fontFamily = "JetBrains Mono",
                layoutStyle = TemplateLayoutStyle.MINIMAL_MONO,
                previewLines = listOf(
                    name.uppercase(),
                    "High-Leverage Priorities & Objectives",
                    "Execution Matrix & Owners",
                    "Review & Retrospective"
                ),
                buildSections = {
                    listOf(
                        DocumentSection(
                            type = SectionType.HEADING,
                            title = name.uppercase(),
                            content = "Owner: Rahul Shah • Cycle: Q4 2026",
                            fontSizeSp = 16,
                            isBold = true
                        ),
                        DocumentSection(
                            type = SectionType.PARAGRAPH,
                            title = "Strategic Focus",
                            content = desc
                        ),
                        DocumentSection(
                            type = SectionType.TABLE,
                            title = "Priority Execution Matrix",
                            content = "Deliverables and targets",
                            tableRows = listOf(
                                listOf("Objective / Task", "Priority", "Target Metric", "Progress"),
                                listOf("Launch Document Scanner 2.0", "P0", "< 1.5s capture latency", "90%"),
                                listOf("Expand Template Library to 112+", "P0", "100% editable sections", "100%"),
                                listOf("Zero-Trust Firestore Sync", "P1", "All security rules verified", "100%")
                            )
                        ),
                        DocumentSection(
                            type = SectionType.BULLET_LIST,
                            title = "Key Takeaways",
                            content = "Eliminate low-impact distractions during morning deep-work blocks\nReview metrics every Friday at 16:00\nArchive completed items weekly"
                        )
                    )
                }
            )
        }
    }

    fun buildCreativeTemplates(): List<DocumentTemplate> {
        val specs = listOf(
            Triple("cre_01", "Portfolio", "Curated showcase of selected design, engineering, or writing works."),
            Triple("cre_02", "Professional Biography", "First-person and third-person speaker bio for conferences and press."),
            Triple("cre_03", "Personal Introduction", "Warm editorial self-introduction for communities, boards, or clients."),
            Triple("cre_04", "Project Case Study", "Challenge, creative process, architecture, and measurable outcomes."),
            Triple("cre_05", "Newsletter", "Editorial digest featuring lead essay, curated links, and community spotlight."),
            Triple("cre_06", "Press Release", "For Immediate Release media announcement with boilerplate and press contact."),
            Triple("cre_07", "Creative Brief", "Campaign objective, target audience, tone of voice, and visual deliverables."),
            Triple("cre_08", "Brand Style Guide", "Typography hierarchy, color palette hex codes, and editorial voice rules."),
            Triple("cre_09", "Podcast / Video Script", "Two-column audio/visual script with intro hook, segments, and call to action."),
            Triple("cre_10", "Exhibition Catalog Sheet", "Artwork metadata, artist statement, medium, and provenance."),
            Triple("cre_11", "Book Proposal Outline", "Synopsis, chapter-by-chapter breakdown, comparable titles, and author platform."),
            Triple("cre_12", "Grant Application Narrative", "Cultural or innovation grant project narrative and community impact.")
        )

        return specs.map { (id, name, desc) ->
            DocumentTemplate(
                id = id,
                name = name,
                category = "Creative",
                description = desc,
                accentHex = "#285B4B",
                fontFamily = "Playfair Display",
                layoutStyle = TemplateLayoutStyle.EDITORIAL_SERIF,
                previewLines = listOf(
                    name.uppercase(),
                    "Editorial Narrative & Concept",
                    "Selected Highlights & Outcomes",
                    "Colophon & Contact"
                ),
                buildSections = {
                    listOf(
                        DocumentSection(
                            type = SectionType.HEADING,
                            title = name.uppercase(),
                            content = "Authored by Rahul Shah • Opporrs Editorial Press",
                            fontSizeSp = 18,
                            isBold = true
                        ),
                        DocumentSection(
                            type = SectionType.PARAGRAPH,
                            title = "1. Creative Vision & Context",
                            content = "$desc Crafted with intentional typography, balanced whitespace, and clear narrative structure."
                        ),
                        DocumentSection(
                            type = SectionType.BULLET_LIST,
                            title = "2. Highlights & Key Works",
                            content = "Opporrs Design System: Warm editorial surfaces with forest green accents (#285B4B)\nCross-Platform Typography: Playfair Display paired with Plus Jakarta Sans\nHuman-Centered Workflows: Seamless transition from camera scan to PDF publication"
                        ),
                        DocumentSection(
                            type = SectionType.PARAGRAPH,
                            title = "3. Colophon & Inquiries",
                            content = "For press kits, collaborations, or speaking inquiries, contact rahul.shah@example.com."
                        )
                    )
                }
            )
        }
    }

    fun buildPastelAndDesignerTemplates(): List<DocumentTemplate> {
        data class DesignerSpec(
            val id: String,
            val name: String,
            val category: String,
            val desc: String,
            val accentHex: String,
            val paperHex: String,
            val fontFamily: String,
            val style: TemplateLayoutStyle,
            val featured: Boolean = false
        )

        val specs = listOf(
            DesignerSpec(
                id = "pastel_01",
                name = "Matcha Sage Executive CV",
                category = "Career",
                desc = "Pastel sage paper sheet paired with Lora serif headings and structured achievement metrics.",
                accentHex = "#2D6A4F",
                paperHex = "#F1F7F4",
                fontFamily = "Lora",
                style = TemplateLayoutStyle.EDITORIAL_SERIF,
                featured = true
            ),
            DesignerSpec(
                id = "pastel_02",
                name = "Rose Quartz Creative Resume",
                category = "Career",
                desc = "Soft blush rose canvas with Cormorant Garamond editorial typography for designers & directors.",
                accentHex = "#9E3F63",
                paperHex = "#FDF4F6",
                fontFamily = "Cormorant Garamond",
                style = TemplateLayoutStyle.MODERN_SIDEBAR,
                featured = true
            ),
            DesignerSpec(
                id = "pastel_03",
                name = "Sky Blue Tech Architect CV",
                category = "Career",
                desc = "Serene sky pastel paper with Space Grotesk geometric headings and engineering skill matrix.",
                accentHex = "#265985",
                paperHex = "#F2F7FC",
                fontFamily = "Space Grotesk",
                style = TemplateLayoutStyle.EXECUTIVE_GRID,
                featured = true
            ),
            DesignerSpec(
                id = "pastel_04",
                name = "Lavender Mist Cover Letter",
                category = "Letters",
                desc = "Refined lavender paper sheet with Merriweather typography for senior leadership applications.",
                accentHex = "#5E4B8B",
                paperHex = "#F6F4FB",
                fontFamily = "Merriweather",
                style = TemplateLayoutStyle.EDITORIAL_SERIF
            ),
            DesignerSpec(
                id = "pastel_05",
                name = "Vintage Parchment Legal Brief",
                category = "Business",
                desc = "Warm vintage parchment paper with classic serif clauses, recitals, and signature block.",
                accentHex = "#5B3E28",
                paperHex = "#F7F1E3",
                fontFamily = "Lora",
                style = TemplateLayoutStyle.CLASSIC_CLEAN
            ),
            DesignerSpec(
                id = "pastel_06",
                name = "Apricot Cream Startup Pitch Memo",
                category = "Business",
                desc = "Warm apricot cream paper for seed/Series A investor memos, traction metrics, and cap table summary.",
                accentHex = "#B55435",
                paperHex = "#FDF6F0",
                fontFamily = "Space Grotesk",
                style = TemplateLayoutStyle.EXECUTIVE_GRID
            ),
            DesignerSpec(
                id = "pastel_07",
                name = "Academic Research Fellowship Dossier",
                category = "Education",
                desc = "Editorial ivory paper with Cormorant Garamond headings for grant committees and postdoctoral boards.",
                accentHex = "#1E4E5D",
                paperHex = "#FAF8F2",
                fontFamily = "Cormorant Garamond",
                style = TemplateLayoutStyle.EDITORIAL_SERIF
            ),
            DesignerSpec(
                id = "pastel_08",
                name = "Pastel Sage Quarterly OKR Tracker",
                category = "Productivity",
                desc = "Calming matcha paper with tabular key result scoring and weekly focus priorities.",
                accentHex = "#2D6A4F",
                paperHex = "#F1F7F4",
                fontFamily = "Plus Jakarta Sans",
                style = TemplateLayoutStyle.EXECUTIVE_GRID
            ),
            DesignerSpec(
                id = "pastel_09",
                name = "Sky Blue Product PRD & Spec",
                category = "Reports",
                desc = "Structured product requirements document with user stories, acceptance criteria, and rollout plan.",
                accentHex = "#265985",
                paperHex = "#F2F7FC",
                fontFamily = "Space Grotesk",
                style = TemplateLayoutStyle.MODERN_SIDEBAR
            ),
            DesignerSpec(
                id = "pastel_10",
                name = "Lavender Wellness & Habit Journal",
                category = "Personal",
                desc = "Soft lavender paper for daily reflections, gratitude tracking, and personal milestone logs.",
                accentHex = "#5E4B8B",
                paperHex = "#F6F4FB",
                fontFamily = "Lora",
                style = TemplateLayoutStyle.CLASSIC_CLEAN
            ),
            DesignerSpec(
                id = "pastel_11",
                name = "Rose Quartz Event & Gala Invitation",
                category = "Creative",
                desc = "Elegant blush rose sheet for formal invitations, program schedules, and VIP RSVP records.",
                accentHex = "#9E3F63",
                paperHex = "#FDF4F6",
                fontFamily = "Cormorant Garamond",
                style = TemplateLayoutStyle.EDITORIAL_SERIF
            ),
            DesignerSpec(
                id = "pastel_12",
                name = "Client Onboarding & Welcome Packet",
                category = "Forms",
                desc = "Warm apricot paper with communication charter, milestone checklist, and billing schedule.",
                accentHex = "#B55435",
                paperHex = "#FDF6F0",
                fontFamily = "Plus Jakarta Sans",
                style = TemplateLayoutStyle.MODERN_SIDEBAR
            ),
            DesignerSpec(
                id = "pastel_13",
                name = "Monospace Engineering Architecture RFC",
                category = "Reports",
                desc = "Technical Request for Comments using JetBrains Mono headers, trade-off matrix, and SLA targets.",
                accentHex = "#1F6E64",
                paperHex = "#F1F7F4",
                fontFamily = "JetBrains Mono",
                style = TemplateLayoutStyle.MINIMAL_MONO
            ),
            DesignerSpec(
                id = "pastel_14",
                name = "International Scholarship Essay & CV",
                category = "Education",
                desc = "Merriweather serif layout crafted for Chevening, Fulbright, and Erasmus scholarship boards.",
                accentHex = "#265985",
                paperHex = "#F2F7FC",
                fontFamily = "Merriweather",
                style = TemplateLayoutStyle.EDITORIAL_SERIF
            ),
            DesignerSpec(
                id = "pastel_15",
                name = "Boutique Consulting Retainer Agreement",
                category = "Business",
                desc = "Refined parchment contract with scope of work, monthly retainer fee table, and dual signatures.",
                accentHex = "#8A6724",
                paperHex = "#F7F1E3",
                fontFamily = "Lora",
                style = TemplateLayoutStyle.CLASSIC_CLEAN
            ),
            DesignerSpec(
                id = "pastel_16",
                name = "Executive Board Resolution Record",
                category = "Letters",
                desc = "Formal corporate board resolution with voting tally table and secretary certification.",
                accentHex = "#3B4254",
                paperHex = "#FAF8F2",
                fontFamily = "Playfair Display",
                style = TemplateLayoutStyle.EDITORIAL_SERIF
            ),
            DesignerSpec(
                id = "pastel_17",
                name = "Design System & UI Audit Checklist",
                category = "Productivity",
                desc = "Comprehensive mobile UI/UX audit matrix covering typography, contrast, touch targets, and states.",
                accentHex = "#5E4B8B",
                paperHex = "#F6F4FB",
                fontFamily = "Space Grotesk",
                style = TemplateLayoutStyle.EXECUTIVE_GRID
            ),
            DesignerSpec(
                id = "pastel_18",
                name = "Creative Director Lookbook & Bio",
                category = "Creative",
                desc = "Editorial monograph sheet combining artistic statement, exhibition history, and press quotes.",
                accentHex = "#9E3F63",
                paperHex = "#FDF4F6",
                fontFamily = "Playfair Display",
                style = TemplateLayoutStyle.EDITORIAL_SERIF
            )
        )

        return specs.map { spec ->
            DocumentTemplate(
                id = spec.id,
                name = spec.name,
                category = spec.category,
                description = spec.desc,
                accentHex = spec.accentHex,
                fontFamily = spec.fontFamily,
                layoutStyle = spec.style,
                paperColorHex = spec.paperHex,
                isFeatured = spec.featured,
                isPopular = true,
                previewLines = listOf(
                    spec.name.uppercase(),
                    "Paper: ${spec.paperHex} • Font: ${spec.fontFamily}",
                    "Structured Editorial Sections & Tables",
                    "Ready for A4 / Letter PDF Export"
                ),
                buildSections = {
                    listOf(
                        DocumentSection(
                            type = SectionType.HEADING,
                            title = spec.name.uppercase(),
                            content = "Prepared by Rahul Shah • ${spec.category} Collection • ${spec.fontFamily}",
                            fontSizeSp = 18,
                            isBold = true
                        ),
                        DocumentSection(
                            type = SectionType.PARAGRAPH,
                            title = "1. Executive Summary & Purpose",
                            content = spec.desc
                        ),
                        DocumentSection(
                            type = SectionType.BULLET_LIST,
                            title = "2. Key Highlights & Deliverables",
                            content = "Crafted on ${spec.paperHex} pastel paper surface with ${spec.accentHex} brand accent\nTypography styled in ${spec.fontFamily} for high readability\nFully customizable sections, tables, and vector signature support"
                        ),
                        DocumentSection(
                            type = SectionType.TABLE,
                            title = "3. Structured Overview Matrix",
                            tableRows = listOf(
                                listOf("Dimension", "Specification", "Status"),
                                listOf("Typography", spec.fontFamily, "Active"),
                                listOf("Paper Surface", spec.paperHex, "Verified"),
                                listOf("Export Readiness", "PDF / TXT / CSV", "Ready")
                            )
                        ),
                        DocumentSection(
                            type = SectionType.SIGNATURE,
                            title = "Authorized Sign-Off",
                            content = "Rahul Shah"
                        )
                    )
                }
            )
        }
    }
}
