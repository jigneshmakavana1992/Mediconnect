package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AuthUiState
import com.example.ui.theme.*

@Composable
fun PatientRegisterScreen(
    authState: AuthUiState,
    onRegister: (
        fullName: String,
        email: String,
        phone: String,
        password: String,
        age: Int,
        gender: String,
        allergies: String,
        chronicConditions: String
    ) -> Unit,
    onBackToLogin: () -> Unit
) {
    val scrollState = rememberScrollState()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("28") }
    var gender by remember { mutableStateOf("Female") }
    var allergies by remember { mutableStateOf("") }
    var chronicConditions by remember { mutableStateOf("") }

    var localError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MedBackgroundLight)
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackToLogin, modifier = Modifier.testTag("register_back_button")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Patient Registration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MedTealDark
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Create your secure Patient EMR Vault & Telemedicine account",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Strict RBAC governance banner
        Surface(
            color = MedTealContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LockClock, contentDescription = null, tint = MedTealPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Zero Self-Registration for Doctors",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedOnTealContainer
                    )
                    Text(
                        text = "Per regulatory compliance, clinical practitioner accounts cannot self-register and must be provisioned via the Institutional Super-Admin.",
                        fontSize = 11.sp,
                        color = MedTealDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (localError != null || authState.errorMessage != null) {
            Surface(
                color = MedCoralContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = localError ?: authState.errorMessage ?: "",
                    color = MedCoralTertiary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Legal Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_fullname_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_email_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_phone_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Create Password (min 6 chars)") },
                    visualTransformation = PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_password_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it },
                        label = { Text("Age") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("register_age_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Column(modifier = Modifier.weight(2f)) {
                        Text(text = "Gender:", fontSize = 11.sp, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Female", "Male", "Other").forEach { g ->
                                FilterChip(
                                    selected = gender == g,
                                    onClick = { gender = g },
                                    label = { Text(g, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Known Drug Allergies (e.g. Penicillin, Sulfa)") },
                    leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MedCoralTertiary) },
                    placeholder = { Text("Leave blank if none") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_allergies_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = chronicConditions,
                    onValueChange = { chronicConditions = it },
                    label = { Text("Chronic Conditions (e.g. Asthma, Hypertension)") },
                    leadingIcon = { Icon(Icons.Default.HealthAndSafety, contentDescription = null) },
                    placeholder = { Text("Leave blank if none") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_chronic_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val age = ageText.toIntOrNull() ?: 25
                        if (fullName.isBlank() || email.isBlank() || password.length < 6) {
                            localError = "Please fill in all required fields (password min 6 chars)."
                        } else {
                            localError = null
                            onRegister(fullName, email, phone, password, age, gender, allergies, chronicConditions)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("register_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                    enabled = !authState.isAuthenticating
                ) {
                    if (authState.isAuthenticating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Create Patient Account", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
