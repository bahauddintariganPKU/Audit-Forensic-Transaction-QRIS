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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.QrisTransaction
import java.util.Locale

@Composable
fun TransactionDetailDialog(
    transaction: QrisTransaction,
    onDismiss: () -> Unit,
    onUpdateStatus: (status: String, notes: String) -> Unit,
    onDelete: () -> Unit
) {
    var selectedStatus by remember { mutableStateOf(transaction.statusInvestigasi) }
    var notesText by remember { mutableStateOf(transaction.catatanAuditor) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("dialog_transaction_detail"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Detail Forensik Transaksi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "RRN: ${transaction.rrn} • ${transaction.waktu}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_detail")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Badge & Nominal Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nominal Transaksi",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            RiskBadge(
                                conclusion = transaction.kesimpulanAudit,
                                score = transaction.totalFraudScore
                            )
                        }

                        Text(
                            text = formatRupiah(transaction.nominal),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (transaction.totalFraudScore >= 2) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "ID Kasir", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = transaction.idKasir, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(text = "ID Konsumen", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = transaction.userIdKonsumen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(text = "Digit Pertama", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "${transaction.digitPertama}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5 Forensic Indicators Breakdown
                Text(
                    text = "Hasil Uji Formula Forensik",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                FormulaInspectionRow(
                    ruleName = "1. Jam Operasional Toko",
                    isTriggered = transaction.flagJamTutup == 1,
                    detail = if (transaction.flagJamTutup == 1)
                        "Pelanggaran: Waktu transaksi jam ${transaction.jam}:00 berada di luar operasional (23:00 - 05:00)"
                    else "Sesuai: Transaksi dalam batas jam buka toko"
                )

                FormulaInspectionRow(
                    ruleName = "2. Transaksi Dipecah (Split Tx)",
                    isTriggered = transaction.flagSplitTx == 1,
                    detail = if (transaction.flagSplitTx == 1) {
                        val selisih = transaction.selisihWaktuMenit?.let { String.format(Locale.US, "%.1f", it) } ?: "< 5"
                        "Pelanggaran: Konsumen sama (${transaction.userIdKonsumen}) melakukan transaksi nominal identik dalam $selisih menit"
                    } else "Sesuai: Tidak ada transaksi split berulang"
                )

                FormulaInspectionRow(
                    ruleName = "3. Gestun / Nominal Bulat",
                    isTriggered = transaction.flagNominalBulat == 1,
                    detail = if (transaction.flagNominalBulat == 1)
                        "Pelanggaran: Nominal >= Rp1.000.000 bulat tanpa pecahan rupiah (indikasi tarik tunai ilegal / gestun)"
                    else "Sesuai: Nominal wajar atau bukan pecahan bulat besar"
                )

                FormulaInspectionRow(
                    ruleName = "4. Kolusi Kasir (Self-Transaction)",
                    isTriggered = transaction.flagSelfTx == 1,
                    detail = if (transaction.flagSelfTx == 1)
                        "Pelanggaran: ID Kasir sama persis dengan User ID Konsumen (${transaction.idKasir}), fraud manipulasi insentif/poin"
                    else "Sesuai: Akun kasir dan pelanggan terpisah"
                )

                FormulaInspectionRow(
                    ruleName = "5. Hukum Benford (Digit Pertama)",
                    isTriggered = transaction.flagBenfordAnomali == 1,
                    detail = if (transaction.flagBenfordAnomali == 1)
                        "Pelanggaran: Nominal > Rp500.000 dengan digit pertama '${transaction.digitPertama}', menyimpang dari kurva alami Benford (rekayasa nominal)"
                    else "Sesuai: Distribusi angka pertama sesuai kaidah alami"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Auditor Investigation Section
                Text(
                    text = "Tindakan Auditor & Status",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Status Chips
                val statusOptions = listOf(
                    QrisTransaction.STATUS_BELUM_DITINJAU,
                    QrisTransaction.STATUS_SEDANG_DIINVESTIGASI,
                    QrisTransaction.STATUS_SELESAI,
                    QrisTransaction.STATUS_FRAUD
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statusOptions.forEach { status ->
                        val isSelected = selectedStatus == status
                        OutlinedButton(
                            onClick = { selectedStatus = status },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_status_${status.lowercase().replace(" ", "_")}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        ) {
                            Text(
                                text = status.split(" ").first(),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Catatan Forensik Auditor") },
                    placeholder = { Text("Masukkan bukti temuan, hasil wawancara kasir, atau tindak lanjut...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_auditor_notes"),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showConfirmDelete = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        modifier = Modifier.testTag("btn_delete_tx")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onUpdateStatus(selectedStatus, notesText)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("btn_save_audit_status")
                    ) {
                        Icon(imageVector = Icons.Default.EditNote, contentDescription = "Simpan", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Audit")
                    }
                }

                if (showConfirmDelete) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Konfirmasi Hapus Transaksi",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Apakah Anda yakin ingin menghapus transaksi ${transaction.rrn} dari basis data?",
                                fontSize = 11.sp,
                                color = Color(0xFF991B1B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        onDelete()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    modifier = Modifier.testTag("btn_confirm_delete")
                                ) {
                                    Text("Ya, Hapus", fontSize = 12.sp)
                                }
                                OutlinedButton(onClick = { showConfirmDelete = false }) {
                                    Text("Batal", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormulaInspectionRow(
    ruleName: String,
    isTriggered: Boolean,
    detail: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTriggered) Color(0xFFFEF2F2) else Color(0xFFF0FDF4)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (isTriggered) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isTriggered) Color(0xFFDC2626) else Color(0xFF16A34A),
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = ruleName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTriggered) Color(0xFF991B1B) else Color(0xFF166534)
                    )
                    Text(
                        text = if (isTriggered) "FLAG: 1" else "FLAG: 0",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTriggered) Color(0xFFDC2626) else Color(0xFF16A34A)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = detail,
                    fontSize = 11.sp,
                    color = if (isTriggered) Color(0xFFB91C1C) else Color(0xFF15803D),
                    lineHeight = 15.sp
                )
            }
        }
    }
}
