package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QrisTransaction
import com.example.ui.components.formatRupiah

@Composable
fun ReportScreen(
    transactions: List<QrisTransaction>,
    csvContent: String,
    onPushToGitHubClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Laporan Utama, 1: Rincian Indikator
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var copyNotice by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_report")
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Title
        Text(
            text = "Laporan Forensik QRIS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Output Sesuai Format Skrip hasil_audit_forensik_qris.csv",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Export & Push Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    shareCsvFile(context, csvContent)
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_export_csv_share"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ekspor CSV", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = onPushToGitHubClick,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_report_push_github"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Push ke Git", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(csvContent))
                    copyNotice = true
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_copy_csv_clipboard"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (copyNotice) "Disalin!" else "Salin CSV", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Laporan Utama", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_laporan_utama")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Rincian Indikator", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_rincian_indikator")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Table Content
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            val horizontalScrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .horizontalScroll(horizontalScrollState)
            ) {
                if (selectedTab == 0) {
                    // TAB 1: LAPORAN UTAMA AUDIT FORENSIK QRIS
                    // Columns: RRN, ID_Kasir, User_ID_Konsumen, Nominal, Total_Fraud_Score, Kesimpulan_Audit
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell("RRN", width = 85.dp, isHeader = true)
                        TableCell("ID Kasir", width = 85.dp, isHeader = true)
                        TableCell("User ID Konsumen", width = 120.dp, isHeader = true)
                        TableCell("Nominal", width = 110.dp, isHeader = true)
                        TableCell("Skor", width = 55.dp, isHeader = true)
                        TableCell("Kesimpulan Audit", width = 210.dp, isHeader = true)
                    }

                    HorizontalDivider(color = DividerDefaults.color)

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        itemsIndexed(transactions) { index, tx ->
                            val isEven = index % 2 == 0
                            val rowBg = if (isEven) Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                            val isHighRisk = tx.totalFraudScore >= 2

                            Row(
                                modifier = Modifier
                                    .background(rowBg)
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCell(tx.rrn, width = 85.dp, isBold = true)
                                TableCell(tx.idKasir, width = 85.dp)
                                TableCell(tx.userIdKonsumen, width = 120.dp)
                                TableCell(formatRupiah(tx.nominal), width = 110.dp, isBold = true)
                                TableCell(
                                    "${tx.totalFraudScore}",
                                    width = 55.dp,
                                    textColor = if (isHighRisk) Color(0xFFDC2626) else if (tx.totalFraudScore == 1) Color(0xFFD97706) else Color(0xFF16A34A),
                                    isBold = true
                                )
                                TableCell(
                                    tx.kesimpulanAudit,
                                    width = 210.dp,
                                    textColor = if (isHighRisk) Color(0xFFDC2626) else if (tx.totalFraudScore == 1) Color(0xFFD97706) else Color(0xFF16A34A),
                                    isBold = isHighRisk
                                )
                            }
                            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.3f))
                        }
                    }
                } else {
                    // TAB 2: RINCIAN INDIKATOR TEMUAN KECURANGAN
                    // Columns: RRN, Flag_Jam_Tutup, Flag_Split_Tx, Flag_Nominal_Bulat, Flag_Self_Tx, Flag_Benford_Anomali
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell("RRN", width = 85.dp, isHeader = true)
                        TableCell("Jam Tutup", width = 85.dp, isHeader = true)
                        TableCell("Split Tx", width = 85.dp, isHeader = true)
                        TableCell("Nominal Bulat", width = 100.dp, isHeader = true)
                        TableCell("Self Tx", width = 85.dp, isHeader = true)
                        TableCell("Benford Anomali", width = 115.dp, isHeader = true)
                        TableCell("Total Skor", width = 75.dp, isHeader = true)
                    }

                    HorizontalDivider(color = DividerDefaults.color)

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        itemsIndexed(transactions) { index, tx ->
                            val isEven = index % 2 == 0
                            val rowBg = if (isEven) Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)

                            Row(
                                modifier = Modifier
                                    .background(rowBg)
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCell(tx.rrn, width = 85.dp, isBold = true)
                                FlagCell(tx.flagJamTutup, width = 85.dp)
                                FlagCell(tx.flagSplitTx, width = 85.dp)
                                FlagCell(tx.flagNominalBulat, width = 100.dp)
                                FlagCell(tx.flagSelfTx, width = 85.dp)
                                FlagCell(tx.flagBenfordAnomali, width = 115.dp)
                                TableCell(
                                    "${tx.totalFraudScore}",
                                    width = 75.dp,
                                    textColor = if (tx.totalFraudScore >= 2) Color(0xFFDC2626) else if (tx.totalFraudScore == 1) Color(0xFFD97706) else Color(0xFF16A34A),
                                    isBold = true
                                )
                            }
                            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
    isBold: Boolean = false,
    textColor: Color? = null
) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        fontSize = if (isHeader) 11.sp else 11.sp,
        fontWeight = if (isHeader || isBold) FontWeight.Bold else FontWeight.Normal,
        color = textColor ?: if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        fontFamily = if (isBold && !isHeader) FontFamily.Monospace else FontFamily.Default
    )
}

@Composable
fun FlagCell(
    flagValue: Int,
    width: androidx.compose.ui.unit.Dp
) {
    val isFlagged = flagValue == 1
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = if (isFlagged) "1 [ALERT]" else "0",
            fontSize = 11.sp,
            fontWeight = if (isFlagged) FontWeight.Bold else FontWeight.Normal,
            color = if (isFlagged) Color(0xFFDC2626) else Color.Gray,
            fontFamily = FontFamily.Monospace
        )
    }
}

fun shareCsvFile(context: Context, csvText: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, csvText)
        putExtra(Intent.EXTRA_TITLE, "hasil_audit_forensik_qris.csv")
        putExtra(Intent.EXTRA_SUBJECT, "Laporan Forensik Audit Transaksi QRIS")
        type = "text/csv"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Ekspor hasil_audit_forensik_qris.csv")
    context.startActivity(shareIntent)
}
