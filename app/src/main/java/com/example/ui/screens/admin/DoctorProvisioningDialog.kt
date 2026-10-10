package com.example.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun DoctorProvisioningDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        temporaryPassword: String,
        councilRegistrationNumber: String,
        qualifications: String,
        speciality: String,
        clinicAffiliation: String,
        consultationFee: Double,
        consultationTimings: String
    ) -> Unit
) {
    val scrollState = rememberScrollState()

    var firstName by remember { mutableStateOf("Elena") }
    var lastName by remember { mutableStateOf("Rostova") }
    var email by remember { mutableStateOf("dr.rostova@medconnect.pro") }
    var phone by remember { mutableStateOf("+1 (555) 234-5678") }
    var temporaryPassword by remember { mutableStateOf("DoctorPass@2026") }
    var councilRegNumber by remember { mutableStateOf("NMC-2021-99412") }
    var qualifications by remember { mutableStateOf("MBBS, MD (General Medicine)") }
    var speciality by remember { mutableStateOf("Internal Medicine & Critical Care") }
    var clinicAffiliation by remember { mutableStateOf("St. Jude Medical Institute") }
    var feeText by remember { mutableStateOf("60.0") }
    var timings by remember { mutableStateOf("Mon-Fri 10:00 - 18:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Provision Clinical Doctor Account",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MedTealDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "Admin Provisioning: An activation credential set will be issued with mandatory first-login password reset.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        modifier = Modifier.weight(1f).testTag("provision_firstname_input")
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        modifier = Modifier.weight(1f).testTag("provision_lastname_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Doctor Institutional Email") },
                    modifier = Modifier.fillMaxWidth().testTag("provision_email_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Verified Phone Number") },
                    modifier = Modifier.fillMaxWidth().testTag("provision_phone_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = temporaryPassword,
                    onValueChange = { temporaryPassword = it },
                    label = { Text("Temporary Password (One-Time)") },
                    modifier = Modifier.fillMaxWidth().testTag("provision_password_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = councilRegNumber,
                    onValueChange = { councilRegNumber = it },
                    label = { Text("Medical Council Reg No (NMC/GMC)") },
                    leadingIcon = { Icon(Icons.Default.Verified, contentDescription = null, tint = MedSuccess) },
                    modifier = Modifier.fillMaxWidth().testTag("provision_council_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = qualifications,
                    onValueChange = { qualifications = it },
                    label = { Text("Medical Qualifications (e.g. MBBS, MD)") },
                    modifier = Modifier.fillMaxWidth().testTag("provision_qualifications_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = speciality,
                    onValueChange = { speciality = it },
                    label = { Text("Clinical Speciality") },
                    modifier = Modifier.fillMaxWidth().testTag("provision_speciality_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = clinicAffiliation,
                    onValueChange = { clinicAffiliation = it },
                    label = { Text("Clinic / Hospital Affiliation") },
                    modifier = Modifier.fillMaxWidth().testTag("provision_affiliation_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = feeText,
                        onValueChange = { feeText = it },
                        label = { Text("Fee ($)") },
                        modifier = Modifier.weight(1f).testTag("provision_fee_input")
                    )
                    OutlinedTextField(
                        value = timings,
                        onValueChange = { timings = it },
                        label = { Text("Timings") },
                        modifier = Modifier.weight(2f).testTag("provision_timings_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fee = feeText.toDoubleOrNull() ?: 50.0
                    onConfirm(
                        firstName,
                        lastName,
                        email,
                        phone,
                        temporaryPassword,
                        councilRegNumber,
                        qualifications,
                        speciality,
                        clinicAffiliation,
                        fee,
                        timings
                    )
                },
                modifier = Modifier.testTag("submit_provision_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary)
            ) {
                Text("Issue Credentials & Verify")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
