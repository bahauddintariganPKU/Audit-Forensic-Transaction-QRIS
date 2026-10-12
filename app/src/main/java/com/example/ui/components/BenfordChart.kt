package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BenfordDistribution
import java.util.Locale

@Composable
fun BenfordAnalysisCard(
    distributions: List<BenfordDistribution>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("benford_chart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Distribusi Hukum Benford (Digit Pertama)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Perbandingan Teoritis vs Aktual Dataset",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Aktual", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.Gray.copy(alpha = 0.4f))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Teori", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bars for digits 1-9
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                distributions.forEach { item ->
                    val isAnomalyCandidate = item.digit in listOf(7, 8, 9)
                    val barColor = if (isAnomalyCandidate && item.actualPercent > item.expectedPercent) {
                        Color(0xFFDC2626) // Highlight anomaly in red
                    } else {
                        MaterialTheme.colorScheme.primary
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.height(65.dp)
                        ) {
                            // Actual bar
                            val actualHeightFraction = (item.actualPercent / 45.0).coerceIn(0.05, 1.0).toFloat()
                            Box(
                                modifier = Modifier
                                    .width(7.dp)
                                    .fillMaxHeight(actualHeightFraction)
                                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                    .background(barColor)
                            )

                            // Expected bar
                            val expectedHeightFraction = (item.expectedPercent / 45.0).coerceIn(0.05, 1.0).toFloat()
                            Box(
                                modifier = Modifier
                                    .width(7.dp)
                                    .fillMaxHeight(expectedHeightFraction)
                                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                    .background(Color.Gray.copy(alpha = 0.35f))
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${item.digit}",
                            fontSize = 11.sp,
                            fontWeight = if (isAnomalyCandidate) FontWeight.Bold else FontWeight.Medium,
                            color = if (isAnomalyCandidate) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFEF3C7))
                    .padding(8.dp)
            ) {
                Text(
                    text = "💡 Catatan Forensik: Angka dengan digit pertama 7, 8, 9 dengan nominal besar (> Rp500.000) ditandai anomali karena kecenderungan rekayasa manual manusia melanggar kurva logaritmik Benford.",
                    fontSize = 10.sp,
                    color = Color(0xFF92400E),
                    lineHeight = 14.sp
                )
            }
        }
    }
}
