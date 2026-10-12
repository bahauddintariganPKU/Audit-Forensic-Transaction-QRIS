package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ForensicSummary

@Composable
fun SummaryMetricGrid(
    summary: ForensicSummary,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Main Total Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("summary_main_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Transaksi Diaudit",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${summary.totalTransaksi} Transaksi",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Total Volume: ${formatRupiah(summary.totalNominal)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "Transactions",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Three Risk Level Cards in a Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RiskMetricSmallCard(
                title = "Risiko Tinggi",
                count = summary.countRisikoTinggi,
                icon = Icons.Default.Error,
                accentColor = Color(0xFFDC2626),
                bgColor = Color(0xFFFEF2F2),
                modifier = Modifier.weight(1f)
            )

            RiskMetricSmallCard(
                title = "Risiko Rendah",
                count = summary.countRisikoRendah,
                icon = Icons.Default.Warning,
                accentColor = Color(0xFFD97706),
                bgColor = Color(0xFFFFFBEB),
                modifier = Modifier.weight(1f)
            )

            RiskMetricSmallCard(
                title = "Aman / Valid",
                count = summary.countAman,
                icon = Icons.Default.CheckCircle,
                accentColor = Color(0xFF16A34A),
                bgColor = Color(0xFFF0FDF4),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RiskMetricSmallCard(
    title: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("metric_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentColor
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$count",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
fun IndicatorBreakdownSection(
    summary: ForensicSummary,
    onIndicatorClick: (String?) -> Unit,
    selectedIndicator: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("indicator_breakdown_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Temuan Indikator Forensik",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "5 Formula Deteksi Algoritma Audit Digital QRIS",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            IndicatorItemRow(
                name = "Jam Tutup (23:00 - 05:00)",
                code = "Jam Tutup",
                count = summary.flagJamTutupCount,
                icon = Icons.Default.AccessTime,
                color = Color(0xFFEF4444),
                isSelected = selectedIndicator == "Jam Tutup",
                onClick = { onIndicatorClick(if (selectedIndicator == "Jam Tutup") null else "Jam Tutup") }
            )

            IndicatorItemRow(
                name = "Split Tx (Interval <= 5 mnt)",
                code = "Split Tx",
                count = summary.flagSplitTxCount,
                icon = Icons.Default.CallSplit,
                color = Color(0xFFF97316),
                isSelected = selectedIndicator == "Split Tx",
                onClick = { onIndicatorClick(if (selectedIndicator == "Split Tx") null else "Split Tx") }
            )

            IndicatorItemRow(
                name = "Gestun / Nominal Bulat (>= 1jt)",
                code = "Nominal Bulat",
                count = summary.flagNominalBulatCount,
                icon = Icons.Default.MonetizationOn,
                color = Color(0xFFEAB308),
                isSelected = selectedIndicator == "Nominal Bulat",
                onClick = { onIndicatorClick(if (selectedIndicator == "Nominal Bulat") null else "Nominal Bulat") }
            )

            IndicatorItemRow(
                name = "Self-Tx Kasir (Kolusi ID Kasir)",
                code = "Self-Tx",
                count = summary.flagSelfTxCount,
                icon = Icons.Default.AccountCircle,
                color = Color(0xFF8B5CF6),
                isSelected = selectedIndicator == "Self-Tx",
                onClick = { onIndicatorClick(if (selectedIndicator == "Self-Tx") null else "Self-Tx") }
            )

            IndicatorItemRow(
                name = "Anomali Benford (> 500rb, Digit 7,8,9)",
                code = "Benford Anomali",
                count = summary.flagBenfordAnomaliCount,
                icon = Icons.Default.Numbers,
                color = Color(0xFFEC4899),
                isSelected = selectedIndicator == "Benford Anomali",
                onClick = { onIndicatorClick(if (selectedIndicator == "Benford Anomali") null else "Benford Anomali") }
            )
        }
    }
}

@Composable
fun IndicatorItemRow(
    name: String,
    code: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .testTag("indicator_row_$code"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = name,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (count > 0) color.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$count Temuan",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (count > 0) color else Color.Gray
                )
            }
        }
    }
}
