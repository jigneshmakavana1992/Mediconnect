package com.example.ui.screens.patient

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GrowthRecordEntity
import com.example.ui.theme.*

@Composable
fun GrowthChartTrackerScreen(
    growthRecords: List<GrowthRecordEntity>,
    onAddRecord: (ageMonths: Int, weight: Float, height: Float, notes: String) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var ageInput by remember { mutableStateOf("21") }
    var weightInput by remember { mutableStateOf("11.8") }
    var heightInput by remember { mutableStateOf("85.5") }
    var notesInput by remember { mutableStateOf("Eating well, active walking") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MedBackgroundLight)
    ) {
        // Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("growth_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "WHO Pediatric Growth Tracker",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MedTealDark
                        )
                        Text(
                            text = "Standard Percentile Curves (WHO 0-60 Months)",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_growth_record_button")
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = MedTealPrimary)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Dependent Info Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Dependent: Leo Wright (Son)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MedTealDark
                            )
                            Text(
                                text = "Age: 18 Months • Male • Target: 50th WHO Percentile",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }

                        Surface(
                            color = MedSuccessContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Healthy Growth",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // WHO Percentiles Visual Chart Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "WHO Weight-For-Age Percentiles Curve (kg)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MedTealDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Custom Canvas Chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val padding = 20f

                                // Draw reference grid
                                repeat(4) { i ->
                                    val y = padding + i * (h - 2 * padding) / 3
                                    drawLine(
                                        color = Color(0xFFCBD5E1),
                                        start = Offset(padding, y),
                                        end = Offset(w - padding, y),
                                        strokeWidth = 1f
                                    )
                                }

                                // 97th Percentile curve (Upper bound)
                                val p97 = Path().apply {
                                    moveTo(padding, h - padding - 40f)
                                    quadraticBezierTo(w * 0.5f, h * 0.5f, w - padding, padding + 10f)
                                }
                                drawPath(p97, Color(0xFFFCA5A5), style = Stroke(width = 2.5f))

                                // 50th Percentile curve (WHO Median)
                                val p50 = Path().apply {
                                    moveTo(padding, h - padding - 20f)
                                    quadraticBezierTo(w * 0.5f, h * 0.65f, w - padding, padding + 40f)
                                }
                                drawPath(p50, Color(0xFF0288D1), style = Stroke(width = 3f))

                                // 3rd Percentile curve (Lower bound)
                                val p3 = Path().apply {
                                    moveTo(padding, h - padding)
                                    quadraticBezierTo(w * 0.5f, h * 0.82f, w - padding, padding + 75f)
                                }
                                drawPath(p3, Color(0xFFFCD34D), style = Stroke(width = 2.5f))

                                // Plot actual patient points
                                val point1X = padding + (w - 2 * padding) * 0.4f
                                val point1Y = h - padding - 55f
                                drawCircle(Color(0xFF004D40), radius = 6f, center = Offset(point1X, point1Y))

                                val point2X = padding + (w - 2 * padding) * 0.65f
                                val point2Y = h - padding - 75f
                                drawCircle(Color(0xFF004D40), radius = 7f, center = Offset(point2X, point2Y))
                                drawLine(Color(0xFF004D40), Offset(point1X, point1Y), Offset(point2X, point2Y), strokeWidth = 3f)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Chart Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(Color(0xFF004D40), RoundedCornerShape(2.dp)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Leo's Points", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(Color(0xFF0288D1), RoundedCornerShape(2.dp)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("50th WHO Median", fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(Color(0xFFFCA5A5), RoundedCornerShape(2.dp)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("97th Band", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Recorded Log Entries
            item {
                Text(
                    text = "Recorded Milestone Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MedTealDark
                )
            }

            items(growthRecords) { rec ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Age: ${rec.ageInMonths} Months",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MedTealDark
                            )
                            Surface(
                                color = MedCyanContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Weight: ${rec.whoWeightPercentile}th %ile",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MedOnCyanContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Weight: ${rec.weightKg} kg", fontSize = 13.sp, color = Color.Black)
                            Text("Height: ${rec.heightCm} cm (${rec.whoHeightPercentile}th %ile)", fontSize = 13.sp, color = Color.Black)
                        }

                        if (rec.clinicalNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Notes: ${rec.clinicalNotes}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Growth Record Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Log Pediatric Growth Point", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { ageInput = it },
                        label = { Text("Age in Months") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Clinical Milestone Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val age = ageInput.toIntOrNull() ?: 18
                        val weight = weightInput.toFloatOrNull() ?: 11.0f
                        val height = heightInput.toFloatOrNull() ?: 82.0f
                        onAddRecord(age, weight, height, notesInput)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary)
                ) {
                    Text("Save to WHO Chart")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
