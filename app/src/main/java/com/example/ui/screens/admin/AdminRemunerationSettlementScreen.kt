package com.example.ui.screens.admin

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRemunerationSettlementScreen(
    adminUser: UserEntity,
    doctors: List<UserEntity>,
    appointments: List<AppointmentEntity>,
    payoutBatches: List<DoctorPayoutBatchEntity>,
    onProcessMondayPayout: (batchId: String, onComplete: (String) -> Unit) -> Unit,
    onNavigateToPayoutPolicy: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedSubTab by remember { mutableStateOf(0) } // 0: Auto-Calculation & Cases, 1: Scoring Chart & Fixed Rates, 2: Monday Settlement Batches
    
    // Configurable Remuneration Rates Fixed by Super Admin
    var platinumRate by remember { mutableDoubleStateOf(150.0) }
    var goldRate by remember { mutableDoubleStateOf(130.0) }
    var silverRate by remember { mutableDoubleStateOf(120.0) }
    var minDurationMinutesThreshold by remember { mutableIntStateOf(3) }
    var showEditRatesDialog by remember { mutableStateOf(false) }

    // Selected Doctor for detail inspection (default to first doctor)
    val doctorList = doctors.ifEmpty {
        listOf(
            UserEntity(
                id = "doc_1",
                email = "dr.jenkins@medconnect.pro",
                passwordHash = "hash",
                phone = "+91 98111-22334",
                fullName = "Dr. Sarah Jenkins",
                role = UserRole.DOCTOR
            )
        )
    }
    var selectedDoctorId by remember { mutableStateOf(doctorList.first().id) }
    val currentDoctor = doctorList.find { it.id == selectedDoctorId } ?: doctorList.first()

    // Evaluate all cases for the selected doctor
    val doctorAppointments = appointments.filter { it.doctorId == currentDoctor.id || it.doctorId.isEmpty() }
    val evaluatedCases = remember(doctorAppointments, platinumRate, goldRate, silverRate) {
        doctorAppointments.map { apt ->
            DoctorRemunerationCalculator.evaluateCase(
                apt = apt,
                platinumRate = platinumRate,
                goldRate = goldRate,
                silverRate = silverRate
            )
        }
    }

    // Aggregates
    val totalCasesCount = evaluatedCases.size
    val qualifiedCases = evaluatedCases.filter { it.isDurationQualified && it.badgeTier != DoctorBadgeTier.UNQUALIFIED }
    val disqualifiedCases = evaluatedCases.filter { !it.isDurationQualified || it.badgeTier == DoctorBadgeTier.UNQUALIFIED }
    
    val platinumCasesCount = evaluatedCases.count { it.badgeTier == DoctorBadgeTier.PLATINUM }
    val goldCasesCount = evaluatedCases.count { it.badgeTier == DoctorBadgeTier.GOLD }
    val silverCasesCount = evaluatedCases.count { it.badgeTier == DoctorBadgeTier.SILVER }
    val unqualifiedCasesCount = evaluatedCases.count { it.badgeTier == DoctorBadgeTier.UNQUALIFIED }

    val platinumTotalInr = platinumCasesCount * platinumRate
    val goldTotalInr = goldCasesCount * goldRate
    val silverTotalInr = silverCasesCount * silverRate
    val autoCalculatedTotalRemuneration = platinumTotalInr + goldTotalInr + silverTotalInr

    val averageScore = if (evaluatedCases.isNotEmpty()) {
        evaluatedCases.map { it.totalScore }.average().toInt()
    } else 88

    val overallDoctorBadge = when {
        averageScore >= 85 -> DoctorBadgeTier.PLATINUM
        averageScore >= 70 -> DoctorBadgeTier.GOLD
        averageScore >= 50 -> DoctorBadgeTier.SILVER
        else -> DoctorBadgeTier.UNQUALIFIED
    }

    var selectedCaseForInspection by remember { mutableStateOf<CaseRemunerationEvaluation?>(null) }
    var selectedBatchForReceipt by remember { mutableStateOf<DoctorPayoutBatchEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MedBackgroundLight)
    ) {
        // Institutional Super-Admin Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("admin_settlement_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MedTealDark)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Doctor Remuneration & Money Settlement",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MedTealDark
                        )
                    }
                    Text(
                        text = "Strict Super-Admin Authority • Auto Badge Calculation • Weekly Monday Settlement",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                IconButton(
                    onClick = onNavigateToPayoutPolicy,
                    modifier = Modifier.testTag("admin_settlement_policy_button")
                ) {
                    Icon(Icons.Default.Gavel, contentDescription = "Doctor Payout Policy", tint = MedTealPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    color = MedWarningContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "ADMIN ONLY",
                        color = MedWarning,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Sub Tabs
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = Color.White,
            contentColor = MedTealPrimary
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("⚡ Auto-Calculation", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_sub_autocalc")
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("📊 Scoring Chart", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_sub_scoring_chart")
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("🗓️ Monday Batches", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_sub_monday_batches")
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // TAB 0: AUTO-CALCULATION & CASES INSPECTION
            if (selectedSubTab == 0) {
                // Doctor Selection Row
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Select Registered Practitioner for Remuneration Audit",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                doctorList.forEach { doc ->
                                    val isSelected = doc.id == currentDoctor.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedDoctorId = doc.id },
                                        label = {
                                            Text(doc.fullName, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.MedicalServices,
                                                contentDescription = null,
                                                tint = if (isSelected) MedTealPrimary else Color.Gray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MedTealContainer,
                                            selectedLabelColor = MedTealDark
                                        ),
                                        modifier = Modifier.testTag("chip_doctor_${doc.id}")
                                    )
                                }
                            }
                        }
                    }
                }

                // Doctor Badge & Remuneration Executive Summary
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = Color(overallDoctorBadge.badgeColorHex).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.WorkspacePremium,
                                                contentDescription = null,
                                                tint = Color(overallDoctorBadge.badgeColorHex),
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = currentDoctor.fullName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MedTealDark
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = Color(overallDoctorBadge.badgeColorHex),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "${overallDoctorBadge.badgeName.uppercase()} BADGE",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Avg Score: $averageScore/100",
                                                fontSize = 11.sp,
                                                color = Color.DarkGray,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = { showEditRatesDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(),
                                    border = ButtonDefaults.outlinedButtonBorder,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("admin_adjust_rates_btn")
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Fix Rates", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(14.dp))

                            // Total Auto Calculated Remuneration Hero Display
                            Surface(
                                color = MedTealContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "AUTO-CALCULATED DOCTOR REMUNERATION",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MedTealDark
                                            )
                                            Text(
                                                text = "₹${String.format(Locale.US, "%,.0f", autoCalculatedTotalRemuneration)}",
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MedTealPrimary
                                            )
                                        }

                                        Surface(
                                            color = MedSuccessContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("100% Quality Audited", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MedSuccess)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Basis: Rating (40 pts) + Call Duration (≥3 min, 30 pts) + Audio/Video Stream (15 pts) + Network Quality (15 pts)",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Badge Tiers Calculation Breakdown Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Platinum Box
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                                    border = BorderStroke(1.dp, Color(0xFFDDD6FE)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("💎 Platinum", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6D28D9))
                                        }
                                        Text("$platinumCasesCount Cases", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        Text("@ ₹${platinumRate.toInt()}/case", fontSize = 10.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("₹${platinumTotalInr.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF6D28D9))
                                    }
                                }

                                // Gold Box
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("🥇 Gold", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                        }
                                        Text("$goldCasesCount Cases", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        Text("@ ₹${goldRate.toInt()}/case", fontSize = 10.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("₹${goldTotalInr.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB45309))
                                    }
                                }

                                // Silver Box
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("🥈 Silver", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                        }
                                        Text("$silverCasesCount Cases", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        Text("@ ₹${silverRate.toInt()}/case", fontSize = 10.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("₹${silverTotalInr.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF334155))
                                    }
                                }

                                // Disqualified (<3 mins) Box
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("❌ < 3 Min", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                        }
                                        Text("$unqualifiedCasesCount Cases", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        Text("Disqualified", fontSize = 10.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("₹0", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626))
                                    }
                                }
                            }
                        }
                    }
                }

                // Section Header: Individual Case Evaluations
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Individual Consultation Cases (${evaluatedCases.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MedTealDark
                        )
                        Text(
                            text = "Tap case for scoring breakdown",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Cases List
                items(evaluatedCases) { evaluation ->
                    CaseEvaluationItemCard(
                        evaluation = evaluation,
                        onClick = { selectedCaseForInspection = evaluation }
                    )
                }

                // Bottom Action Button: Settle Weekly Cohort
                item {
                    Button(
                        onClick = {
                            Toast.makeText(
                                context,
                                "✅ Weekly Settlement of ₹${autoCalculatedTotalRemuneration.toInt()} queued for ${currentDoctor.fullName} (Scheduled for Monday release after 30-day hold).",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_approve_settlement_batch_btn")
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Approve & Queue Weekly Settlement Batch (₹${autoCalculatedTotalRemuneration.toInt()})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // TAB 1: SCORING CHART OF EACH PARAMETER & FIXED RATES
            if (selectedSubTab == 1) {
                // Scoring Philosophy Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MedTealContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = MedTealPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Clinical Quality & Remuneration Framework",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MedTealDark
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Doctor remuneration per case is determined by 4 objective quality parameters. To guarantee clinical thoroughness, any consultation under 3 minutes is disqualified from payment. Highest tier performance is awarded Platinum badge (₹$platinumRate/case).",
                                fontSize = 12.sp,
                                color = MedOnTealContainer,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                // Parameter 1: Patient Rating (Max 40 Pts)
                item {
                    ParameterChartCard(
                        parameterNumber = "1",
                        title = "Patient Rating Score",
                        weightage = "Weight: 40 Points Max",
                        icon = Icons.Default.Star,
                        iconTint = Color(0xFFF59E0B),
                        criteria = listOf(
                            ScoringRow("4.8 to 5.0 Stars", "40 Points", "Tier 1: Exceptional Patient Satisfaction"),
                            ScoringRow("4.3 to 4.7 Stars", "34 Points", "Tier 2: High Patient Satisfaction"),
                            ScoringRow("3.8 to 4.2 Stars", "26 Points", "Tier 3: Standard Compliance"),
                            ScoringRow("3.0 to 3.7 Stars", "18 Points", "Tier 4: Minimum Threshold"),
                            ScoringRow("< 3.0 Stars", "10 Points", "Under Review / Quality Warning")
                        )
                    )
                }

                // Parameter 2: Call Duration (Max 30 Pts - Mandatory 3 Min Threshold)
                item {
                    ParameterChartCard(
                        parameterNumber = "2",
                        title = "Call Duration (Mandatory ≥ 3 Minutes)",
                        weightage = "Weight: 30 Points Max",
                        icon = Icons.Default.Timer,
                        iconTint = MedTealPrimary,
                        criteria = listOf(
                            ScoringRow("≥ 8 Minutes", "30 Points", "Comprehensive Teleconsultation"),
                            ScoringRow("5 to 7 Minutes", "25 Points", "Thorough Clinical Examination"),
                            ScoringRow("3 to 4 Minutes", "20 Points", "Minimum Clinical Requirement Met"),
                            ScoringRow("< 3 Minutes", "0 Points (DISQUALIFIED)", "⚠️ Fails mandatory 3-min consultation rule. ₹0 remuneration.")
                        ),
                        isMandatoryRule = true
                    )
                }

                // Parameter 3: Video / Audio Stream Quality (Max 15 Pts)
                item {
                    ParameterChartCard(
                        parameterNumber = "3",
                        title = "Quality of Video or Audio Call",
                        weightage = "Weight: 15 Points Max",
                        icon = Icons.Default.VideoCall,
                        iconTint = Color(0xFF6366F1),
                        criteria = listOf(
                            ScoringRow("HD 1080p Video / Hi-Fi Audio", "15 Points", "Full clarity, lip-sync, pristine medical view"),
                            ScoringRow("720p Video / Crystal Audio", "12 Points", "Stable high-definition streaming"),
                            ScoringRow("Standard Video / Regular Audio", "8 Points", "Acceptable clinical audio/video"),
                            ScoringRow("Degraded / Low Bitrate", "4 Points", "Artifacts or audio drops detected")
                        )
                    )
                }

                // Parameter 4: Network Quality & Latency (Max 15 Pts)
                item {
                    ParameterChartCard(
                        parameterNumber = "4",
                        title = "Network Quality & Latency Stability",
                        weightage = "Weight: 15 Points Max",
                        icon = Icons.Default.Wifi,
                        iconTint = MedSuccess,
                        criteria = listOf(
                            ScoringRow("Latency < 45ms (0% Loss)", "15 Points", "Dedicated high-bandwidth connection"),
                            ScoringRow("Latency 45 - 90ms (<1% Loss)", "12 Points", "Low jitter, reliable connection"),
                            ScoringRow("Latency 90 - 180ms", "8 Points", "Fair stability with minor jitter"),
                            ScoringRow("Latency > 180ms / High Loss", "4 Points", "Sub-optimal network connection")
                        )
                    )
                }

                // Doctor Badge Classification Matrix & Rates Fixed by Super Admin
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Doctor Badge Remuneration Matrix",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MedTealDark
                                    )
                                    Text(
                                        text = "Fixed by Super Admin Authority",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                Button(
                                    onClick = { showEditRatesDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("admin_edit_rates_chart_btn")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Modify Fixed Rates", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Matrix Table
                            BadgeMatrixRow(
                                badgeName = "Platinum",
                                badgeIcon = "💎",
                                scoreRange = "Score ≥ 85 Points",
                                rateInr = platinumRate,
                                badgeColor = Color(0xFF6D28D9),
                                isTopTier = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            BadgeMatrixRow(
                                badgeName = "Gold",
                                badgeIcon = "🥇",
                                scoreRange = "Score 70 to 84 Points",
                                rateInr = goldRate,
                                badgeColor = Color(0xFFB45309)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            BadgeMatrixRow(
                                badgeName = "Silver",
                                badgeIcon = "🥈",
                                scoreRange = "Score 50 to 69 Points",
                                rateInr = silverRate,
                                badgeColor = Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            BadgeMatrixRow(
                                badgeName = "Disqualified",
                                badgeIcon = "❌",
                                scoreRange = "< 3 Mins OR Score < 50",
                                rateInr = 0.0,
                                badgeColor = Color(0xFFDC2626),
                                isDisqualified = true
                            )
                        }
                    }
                }
            }

            // TAB 2: MONDAY SETTLEMENT BATCHES & 30-DAY ESCROW
            if (selectedSubTab == 2) {
                // Explanation Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MedTealContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Weekly Monday Settlement Lifecycle",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MedTealDark
                                    )
                                    Text(
                                        text = "Strict 1-Month (30-Day) Quality Hold Before Disbursement",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                // Batch List
                items(payoutBatches) { batch ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedBatchForReceipt = batch },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = batch.id,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MedTealDark
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${batch.doctorName})",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                val statusColor = when (batch.payoutStatus) {
                                    PayoutBatchStatus.DISBURSED -> MedSuccess
                                    PayoutBatchStatus.MATURED_QUEUED -> MedTealPrimary
                                    PayoutBatchStatus.HOLDING_ESCROW -> Color(0xFFD97706)
                                }
                                Surface(
                                    color = statusColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = when (batch.payoutStatus) {
                                            PayoutBatchStatus.DISBURSED -> "DISBURSED"
                                            PayoutBatchStatus.MATURED_QUEUED -> "QUEUED (NEXT MON)"
                                            PayoutBatchStatus.HOLDING_ESCROW -> "30-DAY ESCROW HOLD"
                                        },
                                        color = statusColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Net Settlement Payout", fontSize = 10.sp, color = Color.Gray)
                                    Text(
                                        "₹${String.format(Locale.US, "%,.0f", batch.netPayoutInr)}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MedTealPrimary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Scheduled Date", fontSize = 10.sp, color = Color.Gray)
                                    Text(batch.scheduledMondayDate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                                }
                            }

                            if (batch.payoutStatus == PayoutBatchStatus.MATURED_QUEUED) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        onProcessMondayPayout(batch.id) { utr ->
                                            Toast.makeText(context, "✅ Monday Settlement Disbursed! UTR: $utr", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("admin_disburse_batch_${batch.id}")
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Execute Super-Admin Monday Disbursement", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG: Super-Admin Edit Remuneration Rates
    if (showEditRatesDialog) {
        var platInput by remember { mutableStateOf(platinumRate.toInt().toString()) }
        var goldInput by remember { mutableStateOf(goldRate.toInt().toString()) }
        var silverInput by remember { mutableStateOf(silverRate.toInt().toString()) }

        Dialog(onDismissRequest = { showEditRatesDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fix Doctor Remuneration Rates",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MedTealDark
                        )
                        IconButton(onClick = { showEditRatesDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        text = "Super-Admin authority overrides per-case payout fixed for each Badge tier. Minimum consultation duration remains strictly enforced at 3 minutes.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = platInput,
                        onValueChange = { platInput = it },
                        label = { Text("Platinum Badge Remuneration (₹ / case)") },
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = Color(0xFF6D28D9)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = goldInput,
                        onValueChange = { goldInput = it },
                        label = { Text("Gold Badge Remuneration (₹ / case)") },
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = Color(0xFFB45309)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = silverInput,
                        onValueChange = { silverInput = it },
                        label = { Text("Silver Badge Remuneration (₹ / case)") },
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = Color(0xFF475569)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                // Reset to default 150 / 130 / 120
                                platinumRate = 150.0
                                goldRate = 130.0
                                silverRate = 120.0
                                showEditRatesDialog = false
                                Toast.makeText(context, "Rates reset to Default (₹150 / ₹130 / ₹120)", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reset Default", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                platinumRate = platInput.toDoubleOrNull() ?: 150.0
                                goldRate = goldInput.toDoubleOrNull() ?: 130.0
                                silverRate = silverInput.toDoubleOrNull() ?: 120.0
                                showEditRatesDialog = false
                                Toast.makeText(context, "✅ Remuneration rates updated and recalculated across all cases!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save & Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // DIALOG: Individual Case Scoring Inspection
    if (selectedCaseForInspection != null) {
        val case = selectedCaseForInspection!!
        Dialog(onDismissRequest = { selectedCaseForInspection = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Case Quality Scoring Audit",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MedTealDark
                            )
                            Text(
                                text = "Case ID: ${case.appointmentId}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        IconButton(onClick = { selectedCaseForInspection = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = Color(case.badgeTier.badgeColorHex).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "AWARDED BADGE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(case.badgeTier.badgeColorHex)
                                )
                                Text(
                                    text = case.badgeTier.badgeName.uppercase(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(case.badgeTier.badgeColorHex)
                                )
                                Text(
                                    text = "Total Score: ${case.totalScore}/100",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "PER-CASE REMUNERATION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "₹${case.remunerationInr.toInt()}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (case.isDurationQualified) MedTealPrimary else Color(0xFFDC2626)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Parameter Breakdown
                    Text(
                        text = "Parameter Scoring Breakdown",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MedTealDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    ScoringBreakdownRow(
                        title = "1. Patient Rating",
                        value = "${case.patientRating} ★",
                        points = "${case.ratingScore} / 40 Pts",
                        isPassed = case.ratingScore >= 20
                    )
                    ScoringBreakdownRow(
                        title = "2. Call Duration (≥3 Min Rule)",
                        value = "${case.durationSeconds / 60}m ${case.durationSeconds % 60}s",
                        points = if (case.isDurationQualified) "${case.durationScore} / 30 Pts" else "0 / 30 Pts (DISQUALIFIED)",
                        isPassed = case.isDurationQualified
                    )
                    ScoringBreakdownRow(
                        title = "3. Video/Audio Quality",
                        value = case.callQuality,
                        points = "${case.callQualityScore} / 15 Pts",
                        isPassed = true
                    )
                    ScoringBreakdownRow(
                        title = "4. Network Quality",
                        value = case.networkQuality,
                        points = "${case.networkScore} / 15 Pts",
                        isPassed = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = case.evaluationNotes,
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { selectedCaseForInspection = null },
                        colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Audit Details", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // DIALOG: Payout Receipt
    if (selectedBatchForReceipt != null) {
        val batch = selectedBatchForReceipt!!
        Dialog(onDismissRequest = { selectedBatchForReceipt = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Super-Admin Settlement Voucher",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MedTealDark
                        )
                        IconButton(onClick = { selectedBatchForReceipt = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Batch: ${batch.id} • ${batch.doctorName}", fontSize = 12.sp, color = Color.DarkGray)
                    Text("Period: ${batch.cohortPeriodLabel}", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = MedTealContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Net Disbursed", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("₹${String.format(Locale.US, "%,.0f", batch.netPayoutInr)}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MedTealPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Payment Method: ${batch.payoutMethod}", fontSize = 11.sp, color = Color.DarkGray)
                    Text("Bank Account: ${batch.bankName} (${batch.bankAccountMasked})", fontSize = 11.sp, color = Color.DarkGray)
                    Text("IFSC Code: ${batch.bankIfsc}", fontSize = 11.sp, color = Color.DarkGray)
                    Text("UPI ID: ${batch.upiVpa}", fontSize = 11.sp, color = Color.DarkGray)
                    Text("UTR Reference: ${batch.utrReference.ifEmpty { "QUEUED-NPCI" }}", fontSize = 11.sp, color = Color.DarkGray)

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { selectedBatchForReceipt = null },
                        colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dismiss Voucher", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CaseEvaluationItemCard(
    evaluation: CaseRemunerationEvaluation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("case_card_${evaluation.appointmentId}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(evaluation.badgeTier.badgeColorHex).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = evaluation.badgeTier.badgeName.uppercase(),
                            color = Color(evaluation.badgeTier.badgeColorHex),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Score: ${evaluation.totalScore}/100",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }

                Surface(
                    color = if (evaluation.isDurationQualified) MedSuccessContainer else Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (evaluation.isDurationQualified) "+₹${evaluation.remunerationInr.toInt()}" else "DISQUALIFIED (₹0)",
                        color = if (evaluation.isDurationQualified) MedSuccess else Color(0xFFDC2626),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = evaluation.patientName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "${evaluation.consultationType} • Duration: ${evaluation.durationSeconds / 60}m ${evaluation.durationSeconds % 60}s",
                        fontSize = 11.sp,
                        color = if (evaluation.isDurationQualified) Color.Gray else Color(0xFFDC2626),
                        fontWeight = if (evaluation.isDurationQualified) FontWeight.Normal else FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${evaluation.patientRating}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Parameter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                QualityMetricChip("Rating: ${evaluation.ratingScore}/40", isPassed = evaluation.ratingScore >= 20)
                QualityMetricChip(
                    "Duration: ${if (evaluation.isDurationQualified) "${evaluation.durationScore}/30" else "0/30 (<3m)"}",
                    isPassed = evaluation.isDurationQualified
                )
                QualityMetricChip("Stream: ${evaluation.callQualityScore}/15", isPassed = true)
                QualityMetricChip("Net: ${evaluation.networkScore}/15", isPassed = true)
            }
        }
    }
}

@Composable
private fun QualityMetricChip(label: String, isPassed: Boolean) {
    Surface(
        color = if (isPassed) Color(0xFFF1F5F9) else Color(0xFFFEE2E2),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isPassed) Color.DarkGray else Color(0xFFDC2626),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun ScoringBreakdownRow(
    title: String,
    value: String,
    points: String,
    isPassed: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Text(value, fontSize = 10.sp, color = Color.Gray)
        }
        Text(
            text = points,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPassed) MedTealPrimary else Color(0xFFDC2626)
        )
    }
}

@Composable
private fun ParameterChartCard(
    parameterNumber: String,
    title: String,
    weightage: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    criteria: List<ScoringRow>,
    isMandatoryRule: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = iconTint.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "$parameterNumber. $title",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MedTealDark
                        )
                        Text(
                            text = weightage,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = iconTint
                        )
                    }
                }

                if (isMandatoryRule) {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "MANDATORY RULE",
                            color = Color(0xFFDC2626),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            criteria.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(row.condition, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(row.description, fontSize = 10.sp, color = Color.Gray)
                    }
                    Surface(
                        color = if (row.points.contains("0")) Color(0xFFFEE2E2) else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = row.points,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (row.points.contains("0")) Color(0xFFDC2626) else MedTealDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class ScoringRow(val condition: String, val points: String, val description: String)

@Composable
private fun BadgeMatrixRow(
    badgeName: String,
    badgeIcon: String,
    scoreRange: String,
    rateInr: Double,
    badgeColor: Color,
    isTopTier: Boolean = false,
    isDisqualified: Boolean = false
) {
    Surface(
        color = badgeColor.copy(alpha = if (isTopTier) 0.12f else 0.06f),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(badgeIcon, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "$badgeName Badge",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = badgeColor
                    )
                    Text(
                        text = scoreRange,
                        fontSize = 10.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Surface(
                color = if (isDisqualified) Color(0xFFDC2626) else badgeColor,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (isDisqualified) "₹0 (Rejected)" else "₹${rateInr.toInt()} per case",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
