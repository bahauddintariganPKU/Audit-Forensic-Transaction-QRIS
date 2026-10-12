package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.github.GitHubConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GitHubSyncScreen(
    config: GitHubConfig,
    isSyncing: Boolean,
    onSaveConfig: (GitHubConfig) -> Unit,
    onSyncNow: (String) -> Unit,
    onTestConnection: ((Result<String>) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var tokenInput by remember(config.token) { mutableStateOf(config.token) }
    var ownerInput by remember(config.owner) { mutableStateOf(config.owner) }
    var repoInput by remember(config.repo) { mutableStateOf(config.repo) }
    var branchInput by remember(config.branch) { mutableStateOf(config.branch) }
    var filePathInput by remember(config.filePath) { mutableStateOf(config.filePath) }
    var autoSyncEnabled by remember(config.autoSyncEnabled) { mutableStateOf(config.autoSyncEnabled) }

    var isTokenVisible by remember { mutableStateOf(false) }
    var commitMessageInput by remember { mutableStateOf("Audit Forensik QRIS: Ekspor transaksi terbaru") }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale("id", "ID"))

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("screen_github_sync")
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Title
        Text(
            text = "Sinkronisasi Repositori GitHub",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Ekspor & Sinkronisasi Otomatis File Audit hasil_audit_forensik_qris.csv",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Connection Status Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_github_status"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (config.lastSyncSuccess) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (config.lastSyncSuccess) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (config.lastSyncSuccess) Icons.Default.CheckCircle else Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = if (config.lastSyncSuccess) Color(0xFF16A34A) else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (config.isConfigured) "${config.owner}/${config.repo}" else "Repositori Belum Dikonfigurasi",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Branch: ${config.branch} • File: ${config.filePath}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (config.lastCommitSha.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "SHA: ${config.lastCommitSha}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Status Terakhir:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = config.lastSyncStatus,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (config.lastSyncSuccess) Color(0xFF166534) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (config.lastSyncTime > 0) {
                        Text(
                            text = dateFormat.format(Date(config.lastSyncTime)),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Manual Sync Action Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Aksi Push & Sinkronisasi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = commitMessageInput,
                    onValueChange = { commitMessageInput = it },
                    label = { Text("Pesan Commit") },
                    placeholder = { Text("Update hasil_audit_forensik_qris.csv") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_commit_message"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onSyncNow(commitMessageInput) },
                    enabled = !isSyncing && config.isConfigured,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_trigger_sync_now"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sedang Mengunggah ke GitHub...")
                    } else {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sinkronisasi ke GitHub Sekarang")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Auto-Sync Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sinkronisasi Otomatis (Real-Time)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Otomatis push hasil_audit_forensik_qris.csv setiap ada perubahan transaksi",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }

                Switch(
                    checked = autoSyncEnabled,
                    onCheckedChange = {
                        autoSyncEnabled = it
                        onSaveConfig(config.copy(autoSyncEnabled = it))
                    },
                    modifier = Modifier.testTag("switch_auto_sync")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Repository Configuration Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Konfigurasi Repositori GitHub",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Gunakan GitHub Personal Access Token dengan izin akses repo",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Token Input
                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = { tokenInput = it },
                    label = { Text("Personal Access Token (PAT)") },
                    placeholder = { Text("ghp_xxxxxxxxxxxx") },
                    visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                            Icon(
                                imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle token visibility"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_gh_token"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Owner & Repo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ownerInput,
                        onValueChange = { ownerInput = it },
                        label = { Text("Owner / Username") },
                        placeholder = { Text("contoh: octocat") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_gh_owner"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = repoInput,
                        onValueChange = { repoInput = it },
                        label = { Text("Nama Repositori") },
                        placeholder = { Text("contoh: audit-qris") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_gh_repo"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // File Path & Branch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = filePathInput,
                        onValueChange = { filePathInput = it },
                        label = { Text("Path File Target") },
                        placeholder = { Text("hasil_audit_forensik_qris.csv") },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("input_gh_filepath"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = branchInput,
                        onValueChange = { branchInput = it },
                        label = { Text("Branch") },
                        placeholder = { Text("main") },
                        modifier = Modifier
                            .weight(0.7f)
                            .testTag("input_gh_branch"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Buttons: Save & Test Connection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isTestingConnection = true
                            testResultText = null
                            val tempConfig = config.copy(
                                token = tokenInput,
                                owner = ownerInput,
                                repo = repoInput,
                                branch = branchInput,
                                filePath = filePathInput
                            )
                            onSaveConfig(tempConfig)
                            onTestConnection { result ->
                                isTestingConnection = false
                                testResultText = result.fold(
                                    onSuccess = { "✅ $it" },
                                    onFailure = { "❌ ${it.localizedMessage}" }
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_test_gh_connection"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text("Uji Koneksi", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val newConfig = config.copy(
                                token = tokenInput,
                                owner = ownerInput,
                                repo = repoInput,
                                branch = branchInput,
                                filePath = filePathInput,
                                autoSyncEnabled = autoSyncEnabled
                            )
                            onSaveConfig(newConfig)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_save_gh_config"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simpan", fontSize = 12.sp)
                    }
                }

                if (testResultText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = testResultText ?: "",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = Color(0xFF92400E),
                    modifier = Modifier
                        .size(18.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Cara Mendapatkan GitHub Token:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                    Text(
                        text = "1. Buka GitHub > Settings > Developer Settings > Personal access tokens (classic / fine-grained)\n" +
                            "2. Centang izin scope 'repo' (Full control of private repositories)\n" +
                            "3. Salin token dan tempelkan ke kolom di atas.",
                        fontSize = 10.sp,
                        color = Color(0xFF92400E),
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
