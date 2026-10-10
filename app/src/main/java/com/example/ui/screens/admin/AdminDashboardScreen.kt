package com.example.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.model.AppFeatureConfigEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.DoctorJoiningApplicationEntity
import com.example.data.model.DoctorProfileEntity
import com.example.data.model.UserEntity
import com.example.ui.screens.policy.PolicyType
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminUser: UserEntity,
    doctorProfiles: List<DoctorProfileEntity>,
    auditLogs: List<AuditLogEntity>,
    featureConfigs: List<AppFeatureConfigEntity> = emptyList(),
    doctorJoiningApplications: List<DoctorJoiningApplicationEntity> = emptyList(),
    complaintsCount: Int = 0,
    onToggleFeature: (key: String, isEnabled: Boolean) -> Unit = { _, _ -> },
    onUpdateFeatureConfig: (key: String, isEnabled: Boolean, configurableValue: String) -> Unit = { _, _, _ -> },
    onAddCustomFeature: (AppFeatureConfigEntity) -> Unit = {},
    onApproveDoctorJoining: (applicationId: String) -> Unit = {},
    onNavigateToComplaintManagement: () -> Unit = {},
    onProvisionDoctor: (
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        tempPass: String,
        councilReg: String,
        qualifications: String,
        speciality: String,
        affiliation: String,
        fee: Double,
        timings: String
    ) -> Unit,
    onNavigateToAuditLogs: () -> Unit,
    onNavigateToRemunerationSettlement: () -> Unit = {},
    onNavigateToPolicy: (PolicyType) -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Super Edit & Features, 1 = Doctors, 2 = Audit Logs
    var showProvisionDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var editingFeature by remember { mutableStateOf<AppFeatureConfigEntity?>(null) }
    var showAddFeatureDialog by remember { mutableStateOf(false) }

    val categories = listOf("ALL", "PAYMENTS", "TELECONSULTATION", "EMR_RX", "PATIENT_CARE", "SECURITY")

    val filteredFeatures = remember(featureConfigs, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") featureConfigs
        else featureConfigs.filter { it.category == selectedCategoryFilter }
    }

    val enabledCount = featureConfigs.count { it.isEnabled }
    val disabledCount = featureConfigs.size - enabledCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MedBackgroundLight)
    ) {
        // Institutional Super Admin Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MedWarningContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MedWarning)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Super-Admin Control Console",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MedTealDark
                        )
                        Text(
                            text = "${adminUser.fullName} (${adminUser.email})",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateToComplaintManagement,
                        modifier = Modifier.testTag("admin_complaints_top_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (complaintsCount > 0) {
                                    Badge(containerColor = MedCoralTertiary) { Text("$complaintsCount") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.SupportAgent, contentDescription = "Complaint Management", tint = Color(0xFF7C3AED))
                        }
                    }
                    IconButton(
                        onClick = onNavigateToRemunerationSettlement,
                        modifier = Modifier.testTag("admin_settlement_top_button")
                    ) {
                        Icon(Icons.Default.AccountBalance, contentDescription = "Doctor Payment Settlement Hub", tint = MedTealPrimary)
                    }
                    IconButton(
                        onClick = { onNavigateToPolicy(PolicyType.TERMS) },
                        modifier = Modifier.testTag("admin_policy_center_button")
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = "Institutional Policies & Terms", tint = MedTealPrimary)
                    }
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("admin_logout_button")) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = MedCoralTertiary)
                    }
                }
            }
        }

        // Section Tabs: Super Edit Section, Doctor Provisioning, Audit Trail, Settlement Hub
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = MedTealPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("⚡ Super Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_super_edit")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Doctors Registry", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_doctor_registry")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Audit Trail", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_admin_audit")
            )
            Tab(
                selected = false,
                onClick = onNavigateToRemunerationSettlement,
                text = { Text("💰 Settlement Hub", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_admin_settlement_hub")
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // TAB 0: Super Edit & Enable/Disable Section
            if (selectedTab == 0) {
                // Executive Settlement Hub Banner
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToRemunerationSettlement() },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = Color(0xFFF5F3FF),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            tint = Color(0xFF7C3AED),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Doctor Payment & Settlement Hub",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MedTealDark
                                    )
                                    Text(
                                        text = "Weekly Monday Payouts • 30-Day Escrow • Auto-Calculated Remuneration",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Button(
                                onClick = onNavigateToRemunerationSettlement,
                                colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_open_settlement_hub")
                            ) {
                                Text("Open Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Super Admin Configurable Helpline Numbers (Doctor & Patient Assistance)
                item {
                    val docHelpline = featureConfigs.find { it.key == "CONFIG_DOCTOR_HELPLINE" }
                    val patHelpline = featureConfigs.find { it.key == "CONFIG_PATIENT_HELPLINE" }
                    var docPhoneInput by remember(docHelpline?.configurableValue) {
                        mutableStateOf(docHelpline?.configurableValue ?: "+91 1800-202-3001")
                    }
                    var patPhoneInput by remember(patHelpline?.configurableValue) {
                        mutableStateOf(patHelpline?.configurableValue ?: "+91 1800-202-3002")
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = MedTealContainer,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.ContactPhone, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Doctor & Patient Assistance Helpline", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedTealDark)
                                        Text("Configured & Managed by Super Admin", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("LIVE CONFIG", color = Color(0xFF15803D), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = docPhoneInput,
                                onValueChange = { docPhoneInput = it },
                                label = { Text("Doctor Helpline Number (To be filled by Super Admin)", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_doctor_helpline_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = patPhoneInput,
                                onValueChange = { patPhoneInput = it },
                                label = { Text("Patient Helpline Number (To be filled by Super Admin)", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_patient_helpline_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        onUpdateFeatureConfig("CONFIG_DOCTOR_HELPLINE", true, docPhoneInput)
                                        onUpdateFeatureConfig("CONFIG_PATIENT_HELPLINE", true, patPhoneInput)
                                        Toast.makeText(context, "Helpline numbers updated and synced with Doctor & Patient dashboards!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("save_helpline_numbers_button")
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Helpline Numbers", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Super Admin: Complaint Management & RM Portal Access Control
                item {
                    val portalConfig = featureConfigs.find { it.key == "FEATURE_COMPLAINT_MANAGEMENT_PORTAL" }
                    val isPortalEnabled = portalConfig?.isEnabled ?: true

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = Color(0xFFF3E8FF),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Complaint Management & RM Portal", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedTealDark)
                                        Text("Upper-Right Corner Access Toggle", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }

                                Switch(
                                    checked = isPortalEnabled,
                                    onCheckedChange = { checked ->
                                        onToggleFeature("FEATURE_COMPLAINT_MANAGEMENT_PORTAL", checked)
                                        Toast.makeText(context, if (checked) "Complaint Management Portal ENABLED in login screen" else "Complaint Management Portal DISABLED in login screen", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("toggle_complaint_portal_switch")
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isPortalEnabled)
                                    "Status: ENABLED. Doctors, Patients, and Relationship Managers can access text complaints and Chat/Call assistance."
                                else
                                    "Status: DISABLED by Super Admin. The upper-right corner login section is locked.",
                                fontSize = 11.sp,
                                color = if (isPortalEnabled) Color(0xFF15803D) else MedCoralTertiary,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onNavigateToComplaintManagement,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open RM Workspace", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Super Edit Header Banner
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
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = MedCyanSecondary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("SUPER EDIT SECTION", color = MedCyanSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Surface(
                                    color = Color(0xFF1E293B),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Root Authorization", color = Color(0xFF94A3B8), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "App Feature Flags & Enable / Disable Controls",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "As Super-Admin, you can modify any app feature, switch modules on or off dynamically, and adjust operational parameters without redeploying code.",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showAddFeatureDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("add_custom_feature_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Custom Feature Flag", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Feature KPI Metrics Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Active Features", fontSize = 11.sp, color = Color.Gray)
                                Text("$enabledCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MedSuccess)
                                Text("Live in App", fontSize = 10.sp, color = MedSuccess)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Disabled", fontSize = 11.sp, color = Color.Gray)
                                Text("$disabledCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MedCoralTertiary)
                                Text("Turned Off", fontSize = 10.sp, color = Color.DarkGray)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Total Modules", fontSize = 11.sp, color = Color.Gray)
                                Text("${featureConfigs.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MedTealDark)
                                Text("Configurable", fontSize = 10.sp, color = MedTealPrimary)
                            }
                        }
                    }
                }

                // Category Filter Chips
                item {
                    Column {
                        Text("Filter by Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MedTealDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.take(3).forEach { cat ->
                                FilterChip(
                                    selected = selectedCategoryFilter == cat,
                                    onClick = { selectedCategoryFilter = cat },
                                    label = { Text(cat, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.drop(3).forEach { cat ->
                                FilterChip(
                                    selected = selectedCategoryFilter == cat,
                                    onClick = { selectedCategoryFilter = cat },
                                    label = { Text(cat, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                    }
                }

                // Feature Items
                items(filteredFeatures) { feature ->
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
                                Surface(
                                    color = if (feature.isEnabled) MedTealContainer else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = feature.category,
                                        color = if (feature.isEnabled) MedTealPrimary else Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (feature.isEnabled) "ENABLED" else "DISABLED",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (feature.isEnabled) MedSuccess else MedCoralTertiary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = feature.isEnabled,
                                        onCheckedChange = { isChecked ->
                                            onToggleFeature(feature.key, isChecked)
                                            Toast.makeText(
                                                context,
                                                "${feature.title} is now ${if (isChecked) "ENABLED" else "DISABLED"}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        modifier = Modifier.testTag("switch_${feature.key}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = feature.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (feature.isEnabled) Color.Black else Color.Gray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = feature.description,
                                fontSize = 12.sp,
                                color = if (feature.isEnabled) Color.DarkGray else Color.Gray,
                                lineHeight = 16.sp
                            )

                            if (feature.configurableValue.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${feature.valueLabel.ifEmpty { "Value" }}: ",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = feature.configurableValue,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MedTealDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { editingFeature = feature },
                                    modifier = Modifier.testTag("edit_${feature.key}"),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Super Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // TAB 1: Doctor Provisioning & Clinical Directory
            if (selectedTab == 1) {
                item {
                    Surface(
                        color = MedTealContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Zero Self-Registration Policy Enforced",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MedOnTealContainer
                                )
                                Text(
                                    text = "All doctor credentials require administrative verification and council number validation before clinical practice.",
                                    fontSize = 11.sp,
                                    color = MedTealDark
                                )
                            }
                            Button(
                                onClick = { showProvisionDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                                modifier = Modifier.testTag("admin_provision_doctor_button")
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Provision Doctor", fontSize = 12.sp)
                            }
                        }
                    }
                }

                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Aadhaar Verified Doctor Applications (${doctorJoiningApplications.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MedTealDark
                            )
                            if (doctorJoiningApplications.isNotEmpty()) {
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${doctorJoiningApplications.count { it.status == "PENDING_APPROVAL" }} Awaiting Audit",
                                        color = Color(0xFFB45309),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (doctorJoiningApplications.isEmpty()) {
                    item {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MedOutlineLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("No pending doctor joining applications.", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                } else {
                    items(doctorJoiningApplications) { app ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("doctor_application_item_${app.id}"),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (app.status == "PENDING_APPROVAL") Color(0xFFFDE68A) else Color(0xFFBBF7D0)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = app.fullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MedTealDark
                                        )
                                        Text(
                                            text = "${app.qualifications} • ${app.speciality}",
                                            fontSize = 12.sp,
                                            color = Color.DarkGray
                                        )
                                    }

                                    Surface(
                                        color = if (app.status == "PENDING_APPROVAL") Color(0xFFFEF3C7) else Color(0xFFDCFCE7),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (app.status == "PENDING_APPROVAL") "PENDING AUDIT" else "APPROVED",
                                            color = if (app.status == "PENDING_APPROVAL") Color(0xFFB45309) else Color(0xFF15803D),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    color = Color(0xFFF0FDF4),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Aadhaar: ${app.aadhaarNumberMasked} (${app.aadhaarHolderName})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Council Reg: ${app.councilRegistrationNumber} (${app.councilName})",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = "Hospital: ${app.clinicOrHospital}, ${app.city} • Exp: ${app.experienceYears} yrs • Fee: ₹${app.consultationFee.toInt()}",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = "Settlement Bank: ${app.bankName} (${app.bankAccountNumber}) IFSC: ${app.bankIfsc}",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )

                                if (app.status == "PENDING_APPROVAL") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = {
                                                onApproveDoctorJoining(app.id)
                                                Toast.makeText(context, "Doctor ${app.fullName} approved and clinical profile activated!", Toast.LENGTH_LONG).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("approve_doctor_app_${app.id}")
                                        ) {
                                            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Approve & Activate Account", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Clinical Practitioner Directory (${doctorProfiles.size} Active)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MedTealDark
                    )
                }

                items(doctorProfiles) { doc ->
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
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = doc.speciality,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MedTealDark
                                    )
                                    Text(
                                        text = doc.qualifications,
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = "Council Registration: ${doc.councilRegistrationNumber}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MedCyanSecondary
                                    )
                                    Text(
                                        text = "Hospital: ${doc.clinicAffiliation}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Bank: ${doc.bankName} (${doc.bankAccountNumber})",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Settlement: ${doc.payoutCycle}",
                                        fontSize = 11.sp,
                                        color = MedTealPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Surface(
                                    color = MedSuccessContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = MedSuccess, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("VERIFIED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MedSuccess)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: Immutable Clinical Audit Trail
            if (selectedTab == 2) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Platform Audit Trail (${auditLogs.size} Events)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MedTealDark
                        )
                        OutlinedButton(onClick = onNavigateToAuditLogs) {
                            Text("Full Audit Screen", fontSize = 11.sp)
                        }
                    }
                }

                items(auditLogs.take(15)) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = log.action,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MedTealPrimary
                                )
                                Text(
                                    text = log.userRole,
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = log.details, fontSize = 11.sp, color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "By: ${log.performedByEmail} • Target: ${log.targetResource}",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: Super Edit Single Feature Dialog
    if (editingFeature != null) {
        val f = editingFeature!!
        var titleInput by remember { mutableStateOf(f.title) }
        var descInput by remember { mutableStateOf(f.description) }
        var valueInput by remember { mutableStateOf(f.configurableValue) }
        var enabledInput by remember { mutableStateOf(f.isEnabled) }

        Dialog(onDismissRequest = { editingFeature = null }) {
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
                            Text("Super Edit Feature", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MedTealDark)
                            Text("Flag Key: ${f.key}", fontSize = 11.sp, color = Color.Gray)
                        }
                        IconButton(onClick = { editingFeature = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Feature Status", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Switch(
                            checked = enabledInput,
                            onCheckedChange = { enabledInput = it },
                            modifier = Modifier.testTag("dialog_switch_${f.key}")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Feature Title") },
                        modifier = Modifier.fillMaxWidth().testTag("input_feature_title")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Description & Policy") },
                        modifier = Modifier.fillMaxWidth().testTag("input_feature_desc")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = valueInput,
                        onValueChange = { valueInput = it },
                        label = { Text("${f.valueLabel.ifEmpty { "Config Value" }} (e.g. 30 days, 10%, ₹499)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_feature_val")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { editingFeature = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onUpdateFeatureConfig(f.key, enabledInput, valueInput)
                                Toast.makeText(context, "Super Edit saved: ${f.key} updated!", Toast.LENGTH_SHORT).show()
                                editingFeature = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                            modifier = Modifier.testTag("save_super_edit_button")
                        ) {
                            Text("Save Feature Configuration")
                        }
                    }
                }
            }
        }
    }

    // Modal: Add Custom Feature Flag Dialog
    if (showAddFeatureDialog) {
        var keyInput by remember { mutableStateOf("FEATURE_") }
        var categoryInput by remember { mutableStateOf("PAYMENTS") }
        var titleInput by remember { mutableStateOf("") }
        var descInput by remember { mutableStateOf("") }
        var valInput by remember { mutableStateOf("") }
        var valLabelInput by remember { mutableStateOf("Config Value") }
        var isEnabledInput by remember { mutableStateOf(true) }

        Dialog(onDismissRequest = { showAddFeatureDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Register Custom Feature Flag", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MedTealDark)
                    Text("Super-Admin dynamic capability provisioner", fontSize = 11.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it.uppercase() },
                        label = { Text("Feature Key (e.g. FEATURE_TELEMETRY)") },
                        modifier = Modifier.fillMaxWidth().testTag("new_feature_key")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Display Title") },
                        modifier = Modifier.fillMaxWidth().testTag("new_feature_title")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = categoryInput,
                        onValueChange = { categoryInput = it.uppercase() },
                        label = { Text("Category (PAYMENTS, TELECONSULTATION, etc.)") },
                        modifier = Modifier.fillMaxWidth().testTag("new_feature_category")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Feature Description") },
                        modifier = Modifier.fillMaxWidth().testTag("new_feature_desc")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = valInput,
                        onValueChange = { valInput = it },
                        label = { Text("Configurable Value (Optional)") },
                        modifier = Modifier.fillMaxWidth().testTag("new_feature_val")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddFeatureDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (keyInput.isNotBlank() && titleInput.isNotBlank()) {
                                    val newFeature = AppFeatureConfigEntity(
                                        key = keyInput.trim(),
                                        category = categoryInput.trim(),
                                        title = titleInput.trim(),
                                        description = descInput.trim(),
                                        isEnabled = isEnabledInput,
                                        configurableValue = valInput.trim(),
                                        valueLabel = valLabelInput.trim()
                                    )
                                    onAddCustomFeature(newFeature)
                                    Toast.makeText(context, "Registered new feature flag ${newFeature.key}", Toast.LENGTH_SHORT).show()
                                    showAddFeatureDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                            modifier = Modifier.testTag("confirm_add_feature_button")
                        ) {
                            Text("Register Flag")
                        }
                    }
                }
            }
        }
    }

    // Provision Doctor Dialog
    if (showProvisionDialog) {
        DoctorProvisioningDialog(
            onDismiss = { showProvisionDialog = false },
            onConfirm = { fName, lName, email, phone, pass, reg, quals, spec, affil, fee, times ->
                onProvisionDoctor(fName, lName, email, phone, pass, reg, quals, spec, affil, fee, times)
                showProvisionDialog = false
            }
        )
    }
}
