package com.example.ui.screens.doctor

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import com.example.data.model.DoctorPayoutBatchEntity
import com.example.data.model.DoctorProfileEntity
import com.example.data.model.PayoutBatchStatus
import com.example.data.model.UserEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorPaymentManagementScreen(
    doctorUser: UserEntity,
    doctorProfile: DoctorProfileEntity?,
    payoutBatches: List<DoctorPayoutBatchEntity>,
    onProcessMondayPayout: (batchId: String, onComplete: (String) -> Unit) -> Unit,
    onUpdateBankDetails: (bankName: String, accountNum: String, ifsc: String, upiVpa: String, pan: String) -> Unit,
    onNavigateToPayoutPolicy: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var selectedBatchForReceipt by remember { mutableStateOf<DoctorPayoutBatchEntity?>(null) }
    var showEditBankDialog by remember { mutableStateOf(false) }

    // Financial Metrics Calculation
    val maturedQueuedBatches = payoutBatches.filter { it.payoutStatus == PayoutBatchStatus.MATURED_QUEUED }
    val escrowBatches = payoutBatches.filter { it.payoutStatus == PayoutBatchStatus.HOLDING_ESCROW }
    val settledBatches = payoutBatches.filter { it.payoutStatus == PayoutBatchStatus.DISBURSED }

    val nextMondayMaturedAmount = maturedQueuedBatches.sumOf { it.netPayoutInr }
    val totalEscrowHeldAmount = escrowBatches.sumOf { it.netPayoutInr }
    val totalDisbursedAmount = settledBatches.sumOf { it.netPayoutInr }

    val nextMondayBatch = maturedQueuedBatches.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MedBackgroundLight)
    ) {
        // Institutional Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("payment_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MedTealDark)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Doctor Payment & Settlement Hub",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MedTealDark
                    )
                    Text(
                        text = "${doctorUser.fullName} • Weekly Monday Cycle (30-Day Escrow)",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Surface(
                    color = MedTealContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Every Mon",
                        color = MedTealPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Tab Selector: 0 = Weekly Pipeline, 1 = Disbursement History, 2 = Bank & UPI Settings
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = MedTealPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Weekly Pipeline", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_weekly_pipeline")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Settlement History", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_settlement_history")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Bank & UPI", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_bank_upi")
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Policy Announcement Banner: Weekly Monday + 1-Month Hold
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MedCyanSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "DOCTOR PAYMENT POLICY",
                                    color = MedCyanSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "30-Day Hold • Mon Payout",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Weekly Monday Settlement with 1-Month Holding Period",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Consultation fees are held in escrow for exactly 30 days to facilitate follow-ups, patient dispute buffers, and quality reviews. Once matured, funds are disbursed every Monday directly to your registered bank account or UPI.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onNavigateToPayoutPolicy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .testTag("view_full_payout_policy_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MedCyanSecondary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MedCyanSecondary)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Read Full Doctor Payout & Escrow Policy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Key Financial Metrics Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Metric 1: Next Monday (Matured)
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Next Monday", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "₹${nextMondayMaturedAmount.toInt()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedTealDark
                            )
                            Text(
                                text = if (nextMondayBatch != null) nextMondayBatch.scheduledMondayDate else "Ready",
                                fontSize = 10.sp,
                                color = MedTealPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Metric 2: 30-Day Escrow Hold
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("30-Day Escrow", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "₹${totalEscrowHeldAmount.toInt()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                            Text(
                                text = "${escrowBatches.size} cohorts maturing",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Metric 3: Total Settled
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Disbursed", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "₹${totalDisbursedAmount.toInt()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedSuccess
                            )
                            Text(
                                text = "100% on Mondays",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Tab 0: Weekly Pipeline (Matured Batch ready for Monday + Upcoming Cohorts in 30-Day Hold)
            if (selectedTab == 0) {
                // Next Monday Settlement Banner & Action
                if (nextMondayBatch != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MedTealContainer.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MedTealPrimary)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Next Monday Settlement Batch",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MedTealDark
                                        )
                                        Text(
                                            text = nextMondayBatch.scheduledMondayDate,
                                            fontSize = 12.sp,
                                            color = MedTealPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Surface(
                                        color = MedSuccessContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "30-Day Hold Completed",
                                            color = MedSuccess,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Gross Earnings (${nextMondayBatch.consultationCount} sessions):", fontSize = 11.sp, color = Color.DarkGray)
                                        Text("Less Platform Tech Fee (10%):", fontSize = 11.sp, color = Color.DarkGray)
                                        Text("Less TDS Deduction u/s 194J (10%):", fontSize = 11.sp, color = Color.DarkGray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Net Doctor Payout (80%):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MedTealDark)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("₹${nextMondayBatch.grossEarningsInr.toInt()}", fontSize = 11.sp, color = Color.DarkGray)
                                        Text("-₹${nextMondayBatch.platformFeeInr.toInt()}", fontSize = 11.sp, color = MedCoralTertiary)
                                        Text("-₹${nextMondayBatch.tdsInr.toInt()}", fontSize = 11.sp, color = MedCoralTertiary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("₹${nextMondayBatch.netPayoutInr.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MedTealPrimary)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        onProcessMondayPayout(nextMondayBatch.id) { resultMsg ->
                                            Toast.makeText(context, resultMsg, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("simulate_monday_settlement_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Simulate Monday Bank Disbursement (NEFT/UPI)", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "30-Day Escrow Pipeline (Upcoming Monday Releases)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MedTealDark
                    )
                }

                items(escrowBatches) { batch ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = batch.cohortPeriodLabel,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MedTealDark
                                )
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${batch.holdingDaysRemaining} Days Hold Remaining",
                                        color = Color(0xFF92400E),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Release Schedule: ${batch.scheduledMondayDate}",
                                fontSize = 11.sp,
                                color = MedTealPrimary,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${batch.consultationCount} consultations • Gross: ₹${batch.grossEarningsInr.toInt()}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "Net: ₹${batch.netPayoutInr.toInt()}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            }

            // Tab 1: Settlement History & Receipts
            if (selectedTab == 1) {
                item {
                    Text(
                        text = "Monday Payout Records & Bank UTR Receipts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MedTealDark
                    )
                }

                if (settledBatches.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No settled disbursements yet. Process upcoming Monday batches to see history.", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                } else {
                    items(settledBatches) { batch ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBatchForReceipt = batch },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = batch.scheduledMondayDate,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MedTealDark
                                        )
                                    }
                                    Surface(
                                        color = MedSuccessContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "DISBURSED",
                                            color = MedSuccess,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Bank UTR: ${batch.utrReference}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = "Credited to: ${batch.bankName} (${batch.bankAccountMasked}) • UPI: ${batch.upiVpa}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${batch.consultationCount} Sessions (30-day hold cleared)",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "₹${batch.netPayoutInr.toInt()}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MedSuccess
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.Receipt, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab 2: Bank & UPI Settlement Settings
            if (selectedTab == 2) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Direct Settlement Bank Details",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MedTealDark
                                )
                                OutlinedButton(
                                    onClick = { showEditBankDialog = true },
                                    modifier = Modifier.testTag("edit_bank_details_button"),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Bank Name:", fontSize = 12.sp, color = Color.Gray)
                                Text(doctorProfile?.bankName ?: "HDFC Bank Ltd", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Account Number:", fontSize = 12.sp, color = Color.Gray)
                                Text(doctorProfile?.bankAccountNumber ?: "•••• •••• •••• 8912", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("IFSC Code:", fontSize = 12.sp, color = Color.Gray)
                                Text(doctorProfile?.bankIfsc ?: "HDFC0001042", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Doctor UPI ID (VPA):", fontSize = 12.sp, color = Color.Gray)
                                Text(doctorProfile?.doctorUpiVpa ?: "dr.jenkins@okicici", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MedTealPrimary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("PAN Card (TDS Sec 194J):", fontSize = 12.sp, color = Color.Gray)
                                Text(doctorProfile?.panNumber ?: "ABCDE1234F", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Settlement Frequency:", fontSize = 12.sp, color = Color.Gray)
                                Text("Every Monday (after 30-day hold)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MedSuccess)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Disbursement Statement & Tax Invoice Receipt
    if (selectedBatchForReceipt != null) {
        val b = selectedBatchForReceipt!!
        Dialog(onDismissRequest = { selectedBatchForReceipt = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Official Payout Advice", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MedTealDark)
                            Text("Batch Ref: ${b.id}", fontSize = 11.sp, color = Color.Gray)
                        }
                        IconButton(onClick = { selectedBatchForReceipt = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Consultation Cohort: ${b.cohortPeriodLabel}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Disbursement Date: ${b.scheduledMondayDate}", fontSize = 12.sp, color = Color.Gray)
                    Text("Bank UTR Reference: ${b.utrReference}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MedTealPrimary)

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Gross Consultations (${b.consultationCount} pts):", fontSize = 11.sp)
                                Text("₹${b.grossEarningsInr.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Less Platform Service Fee (10%):", fontSize = 11.sp, color = MedCoralTertiary)
                                Text("-₹${b.platformFeeInr.toInt()}", fontSize = 11.sp, color = MedCoralTertiary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Less TDS Withheld u/s 194J (10%):", fontSize = 11.sp, color = MedCoralTertiary)
                                Text("-₹${b.tdsInr.toInt()}", fontSize = 11.sp, color = MedCoralTertiary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Net Disbursed Amount:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MedTealDark)
                                Text("₹${b.netPayoutInr.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MedSuccess)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Destination: ${b.bankName} (${b.bankAccountMasked}) • IFSC: ${b.bankIfsc}", fontSize = 11.sp, color = Color.Gray)
                    Text("UPI VPA: ${b.upiVpa}", fontSize = 11.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            Toast.makeText(context, "Payout Advice statement downloaded to device storage", Toast.LENGTH_SHORT).show()
                            selectedBatchForReceipt = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download Official Tax Invoice / Advice")
                    }
                }
            }
        }
    }

    // Modal: Edit Bank & UPI Details
    if (showEditBankDialog) {
        var bankNameInput by remember { mutableStateOf(doctorProfile?.bankName ?: "HDFC Bank Ltd") }
        var accountInput by remember { mutableStateOf(doctorProfile?.bankAccountNumber ?: "9821004812") }
        var ifscInput by remember { mutableStateOf(doctorProfile?.bankIfsc ?: "HDFC0001042") }
        var upiInput by remember { mutableStateOf(doctorProfile?.doctorUpiVpa ?: "dr.jenkins@okicici") }
        var panInput by remember { mutableStateOf(doctorProfile?.panNumber ?: "ABCDE1234F") }

        Dialog(onDismissRequest = { showEditBankDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Edit Settlement Banking Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MedTealDark)
                    Text("Used for Monday weekly payouts after 30-day escrow", fontSize = 11.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = bankNameInput,
                        onValueChange = { bankNameInput = it },
                        label = { Text("Bank Name") },
                        modifier = Modifier.fillMaxWidth().testTag("input_bank_name")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = accountInput,
                        onValueChange = { accountInput = it },
                        label = { Text("Account Number") },
                        modifier = Modifier.fillMaxWidth().testTag("input_account_number")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = ifscInput,
                        onValueChange = { ifscInput = it },
                        label = { Text("IFSC Code") },
                        modifier = Modifier.fillMaxWidth().testTag("input_ifsc_code")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = upiInput,
                        onValueChange = { upiInput = it },
                        label = { Text("Doctor UPI VPA (e.g. dr@okhdfcbank)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_doctor_upi")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = panInput,
                        onValueChange = { panInput = it },
                        label = { Text("PAN Number (For TDS u/s 194J)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_pan_number")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showEditBankDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onUpdateBankDetails(bankNameInput, accountInput, ifscInput, upiInput, panInput)
                                Toast.makeText(context, "Settlement bank details updated successfully!", Toast.LENGTH_SHORT).show()
                                showEditBankDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                            modifier = Modifier.testTag("save_bank_details_button")
                        ) {
                            Text("Save Bank Details")
                        }
                    }
                }
            }
        }
    }
}
