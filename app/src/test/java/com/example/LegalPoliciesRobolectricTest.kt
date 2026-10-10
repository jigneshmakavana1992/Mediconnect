package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ui.screens.policy.LegalPoliciesScreen
import com.example.ui.screens.policy.PolicyType
import com.example.ui.theme.MedConnectProTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class LegalPoliciesRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testPrivacyPolicyRendersByDefault() {
        var backClicked = false
        composeTestRule.setContent {
            MedConnectProTheme {
                LegalPoliciesScreen(
                    initialPolicy = PolicyType.PRIVACY,
                    onBack = { backClicked = true }
                )
            }
        }

        // Verify Top Bar and Tabs
        composeTestRule.onNodeWithText("Institutional Policy Center").assertIsDisplayed()
        composeTestRule.onNodeWithTag("policy_tab_privacy").assertIsDisplayed()
        composeTestRule.onNodeWithTag("policy_tab_terms").assertIsDisplayed()
        composeTestRule.onNodeWithTag("policy_tab_refund").assertIsDisplayed()
        composeTestRule.onNodeWithTag("policy_tab_payout").assertIsDisplayed()

        // Verify Privacy Header Banner
        composeTestRule.onNodeWithText("Privacy Policy").assertIsDisplayed()
        composeTestRule.onNodeWithText("DPDP Act 2023 & DISHA Compliant • End-to-End Health Data Protection").assertIsDisplayed()

        // Verify Section 1
        composeTestRule.onNodeWithTag("policy_sections_list")
            .performScrollToNode(hasText("1. Scope & Healthcare Legislative Compliance"))
        composeTestRule.onNodeWithText("1. Scope & Healthcare Legislative Compliance").assertIsDisplayed()

        // Verify Back Button
        composeTestRule.onNodeWithTag("policy_back_button").performClick()
        assertTrue("Back callback should be invoked", backClicked)
    }

    @Test
    fun testTermsAndConditionsMentionsPlatformNotLiable() {
        composeTestRule.setContent {
            MedConnectProTheme {
                LegalPoliciesScreen(
                    initialPolicy = PolicyType.TERMS,
                    onBack = {}
                )
            }
        }

        // Verify Subtitle displays Intermediary Safe Harbor & No Liability notice
        composeTestRule.onNodeWithText("IT Act Sec 79 Intermediary • Platform Not Liable for Doctor Negligence • Doctor Indemnity Required").assertIsDisplayed()

        // Verify Section 1: Platform Not Liable for Negligence
        composeTestRule.onNodeWithTag("policy_sections_list")
            .performScrollToNode(hasText("1. Platform Intermediary Status & Zero Clinical Liability"))
        composeTestRule.onNodeWithText("1. Platform Intermediary Status & Zero Clinical Liability").assertIsDisplayed()
        composeTestRule.onNodeWithText("ZERO PLATFORM LIABILITY").assertIsDisplayed()

        // Expand Section 1 to read exact terms
        composeTestRule.onNodeWithTag("policy_expand_button_term_1").performClick()
        composeTestRule.onNodeWithText(
            "NO PLATFORM LIABILITY FOR DOCTOR NEGLIGENCE",
            substring = true
        ).assertExists()
    }

    @Test
    fun testMandatoryDoctorProfessionalIndemnityInsuranceClause() {
        composeTestRule.setContent {
            MedConnectProTheme {
                LegalPoliciesScreen(
                    initialPolicy = PolicyType.TERMS,
                    onBack = {}
                )
            }
        }

        // Verify Subtitle mentions Doctor Indemnity Required
        composeTestRule.onNodeWithText("IT Act Sec 79 Intermediary • Platform Not Liable for Doctor Negligence • Doctor Indemnity Required").assertIsDisplayed()

        // Verify Section 2: Mandatory Doctor Professional Indemnity Insurance title and badge
        composeTestRule.onNodeWithTag("policy_sections_list")
            .performScrollToNode(hasText("2. Mandatory Doctor Professional Indemnity Insurance"))
        composeTestRule.onNodeWithText("2. Mandatory Doctor Professional Indemnity Insurance").assertIsDisplayed()
        composeTestRule.onNodeWithText("MANDATORY DOCTOR INSURANCE").assertIsDisplayed()

        // Verify Section 2 summary states doctor must independently procure and maintain indemnity insurance
        composeTestRule.onNodeWithText(
            "Every doctor must independently procure and maintain an active Professional Indemnity Medical Malpractice Insurance policy",
            substring = true
        ).assertIsDisplayed()
    }

    @Test
    fun testRefundPolicyTabDisplaysCancellationAndNoShowRules() {
        composeTestRule.setContent {
            MedConnectProTheme {
                LegalPoliciesScreen(
                    initialPolicy = PolicyType.REFUND,
                    onBack = {}
                )
            }
        }

        // Verify Refund Header
        composeTestRule.onNodeWithText("100% Doctor No-Show Protection • 24-48h UPI Processing").assertIsDisplayed()

        // Verify Section 1: Patient Cancellation Window
        composeTestRule.onNodeWithTag("policy_sections_list")
            .performScrollToNode(hasText("1. Patient Cancellation Window & Fee Schedule"))
        composeTestRule.onNodeWithText("1. Patient Cancellation Window & Fee Schedule").assertIsDisplayed()
        composeTestRule.onNodeWithText("100% REFUND (>2h)").assertIsDisplayed()

        // Verify Section 2: Doctor No-Show Guarantee
        composeTestRule.onNodeWithTag("policy_sections_list")
            .performScrollToNode(hasText("2. Doctor Unavailability & No-Show Guarantee"))
        composeTestRule.onNodeWithText("2. Doctor Unavailability & No-Show Guarantee").assertIsDisplayed()
        composeTestRule.onNodeWithText("100% GUARANTEE").assertIsDisplayed()
    }

    @Test
    fun testDoctorPayoutPolicyTabDisplaysEscrowAndMondayCycle() {
        composeTestRule.setContent {
            MedConnectProTheme {
                LegalPoliciesScreen(
                    initialPolicy = PolicyType.PAYOUT,
                    onBack = {}
                )
            }
        }

        // Verify Payout Header
        composeTestRule.onNodeWithText("Automated Monday Settlements • 30-Day Dispute Escrow Holding").assertIsDisplayed()

        // Verify Weekly Monday and 30-Day Escrow Sections
        composeTestRule.onNodeWithTag("policy_sections_list")
            .performScrollToNode(hasText("2. Weekly Monday Settlement Cycle"))
        composeTestRule.onNodeWithText("2. Weekly Monday Settlement Cycle").assertIsDisplayed()
        composeTestRule.onNodeWithText("EVERY MONDAY 06:00").assertIsDisplayed()

        composeTestRule.onNodeWithTag("policy_sections_list")
            .performScrollToNode(hasText("3. Mandatory 30-Day Escrow Holding Period"))
        composeTestRule.onNodeWithText("3. Mandatory 30-Day Escrow Holding Period").assertIsDisplayed()
        composeTestRule.onNodeWithText("30-DAY ESCROW").assertIsDisplayed()

        // Verify TDS Section
        composeTestRule.onNodeWithTag("policy_sections_list")
            .performScrollToNode(hasText("5. Tax Deducted at Source (TDS) Compliance"))
        composeTestRule.onNodeWithText("5. Tax Deducted at Source (TDS) Compliance").assertIsDisplayed()
        composeTestRule.onNodeWithText("10% SEC 194J TDS").assertIsDisplayed()
    }

    @Test
    fun testSearchFilteringAndAcknowledgment() {
        composeTestRule.setContent {
            MedConnectProTheme {
                LegalPoliciesScreen(
                    initialPolicy = PolicyType.TERMS,
                    onBack = {}
                )
            }
        }

        // Type search query "negligence"
        composeTestRule.onNodeWithTag("policy_search_input").performTextInput("negligence")

        // Matches should be displayed
        composeTestRule.onNodeWithTag("policy_sections_list")
            .performScrollToNode(hasText("1. Platform Intermediary Status & Zero Clinical Liability"))
        composeTestRule.onNodeWithText("1. Platform Intermediary Status & Zero Clinical Liability").assertIsDisplayed()

        // Scroll to acknowledgment button and click
        composeTestRule.onNodeWithTag("policy_sections_list")
            .performScrollToNode(hasTestTag("policy_acknowledge_button"))
        composeTestRule.onNodeWithTag("policy_acknowledge_button").performClick()
        composeTestRule.onNodeWithText("Acknowledged ✓", substring = true).assertIsDisplayed()
    }
}
