package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.QrisTransaction
import com.example.notification.ForensicNotificationHelper
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.RealtimeAlertsSheet
import com.example.ui.components.TransactionDetailDialog
import com.example.ui.screens.AuditorProfileScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GitHubSyncScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AuditViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ForensicNotificationHelper.initNotificationChannels(this)

        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(auditViewModel: AuditViewModel = viewModel()) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Request notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Observe ViewModel states
    val transactions by auditViewModel.transactions.collectAsStateWithLifecycle()
    val filteredTransactions by auditViewModel.filteredTransactions.collectAsStateWithLifecycle()
    val summary by auditViewModel.summary.collectAsStateWithLifecycle()
    val auditLogs by auditViewModel.auditLogs.collectAsStateWithLifecycle()
    val realtimeAlerts by auditViewModel.realtimeAlerts.collectAsStateWithLifecycle()
    val gitHubConfig by auditViewModel.gitHubConfig.collectAsStateWithLifecycle()
    val activeAuditor by auditViewModel.activeAuditor.collectAsStateWithLifecycle()
    val isSyncing by auditViewModel.isSyncing.collectAsStateWithLifecycle()
    val userMessage by auditViewModel.userMessage.collectAsStateWithLifecycle()

    val searchQuery by auditViewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedRiskFilter by auditViewModel.selectedRiskFilter.collectAsStateWithLifecycle()
    val selectedIndicatorFilter by auditViewModel.selectedIndicatorFilter.collectAsStateWithLifecycle()

    // Screen navigation state (0: Dashboard, 1: Transaksi, 2: Laporan, 3: GitHub, 4: Auditor)
    var currentTabIndex by remember { mutableIntStateOf(0) }

    // Dialog & Sheet States
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTransactionForDetail by remember { mutableStateOf<QrisTransaction?>(null) }
    var showAlertsSheet by remember { mutableStateOf(false) }

    // Handle back button: if not on Dashboard, return to Dashboard first
    BackHandler(enabled = currentTabIndex != 0) {
        currentTabIndex = 0
    }

    // Show Snackbar on user messages
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            auditViewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Audit Forensik QRIS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { currentTabIndex = 4 }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF16A34A))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = activeAuditor.name,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                },
                actions = {
                    // GitHub status icon
                    IconButton(
                        onClick = { currentTabIndex = 3 },
                        modifier = Modifier.testTag("btn_top_github_status")
                    ) {
                        Icon(
                            imageVector = if (gitHubConfig.lastSyncSuccess) Icons.Default.CloudDone else Icons.Default.CloudSync,
                            contentDescription = "GitHub Sync",
                            tint = if (gitHubConfig.lastSyncSuccess) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Notification Bell with badge for alerts
                    IconButton(
                        onClick = { showAlertsSheet = true },
                        modifier = Modifier.testTag("btn_top_notification_bell")
                    ) {
                        BadgedBox(
                            badge = {
                                if (realtimeAlerts.isNotEmpty()) {
                                    val highRiskCount = realtimeAlerts.count { it.isHighRisk }
                                    Badge(
                                        containerColor = if (highRiskCount > 0) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(text = "${realtimeAlerts.size.coerceAtMost(99)}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (summary.countRisikoTinggi > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "Peringatan",
                                tint = if (summary.countRisikoTinggi > 0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTabIndex == 0,
                    onClick = { currentTabIndex = 0 },
                    icon = {
                        Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Dashboard")
                    },
                    label = { Text("Dashboard", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                NavigationBarItem(
                    selected = currentTabIndex == 1,
                    onClick = { currentTabIndex = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (summary.countRisikoTinggi > 0) {
                                    Badge(containerColor = Color(0xFFDC2626)) {
                                        Text("${summary.countRisikoTinggi}")
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = "Transaksi")
                        }
                    },
                    label = { Text("Transaksi", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_transaksi")
                )

                NavigationBarItem(
                    selected = currentTabIndex == 2,
                    onClick = { currentTabIndex = 2 },
                    icon = {
                        Icon(imageVector = Icons.Default.Assessment, contentDescription = "Laporan")
                    },
                    label = { Text("Laporan", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_laporan")
                )

                NavigationBarItem(
                    selected = currentTabIndex == 3,
                    onClick = { currentTabIndex = 3 },
                    icon = {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = "GitHub")
                    },
                    label = { Text("GitHub", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_github")
                )

                NavigationBarItem(
                    selected = currentTabIndex == 4,
                    onClick = { currentTabIndex = 4 },
                    icon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Auditor")
                    },
                    label = { Text("Auditor", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_auditor")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTabIndex) {
                0 -> {
                    DashboardScreen(
                        summary = summary,
                        highRiskTransactions = transactions.filter { it.totalFraudScore >= 2 },
                        onNavigateToTransactions = { riskFilter ->
                            if (riskFilter != null) {
                                auditViewModel.setRiskFilter(riskFilter)
                            }
                            currentTabIndex = 1
                        },
                        onNavigateToGitHub = { currentTabIndex = 3 },
                        onNavigateToReport = { currentTabIndex = 2 },
                        onAddTransactionClick = { showAddDialog = true },
                        onSimulateScenario = { scenario -> auditViewModel.simulateLiveTransaction(scenario) },
                        onResetData = { auditViewModel.resetToSampleData() },
                        selectedIndicator = selectedIndicatorFilter,
                        onSelectIndicator = { ind -> auditViewModel.setIndicatorFilter(ind) }
                    )
                }
                1 -> {
                    TransactionsScreen(
                        transactions = filteredTransactions,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { auditViewModel.setSearchQuery(it) },
                        selectedRiskFilter = selectedRiskFilter,
                        onRiskFilterChange = { auditViewModel.setRiskFilter(it) },
                        selectedIndicatorFilter = selectedIndicatorFilter,
                        onIndicatorFilterChange = { auditViewModel.setIndicatorFilter(it) },
                        onTransactionClick = { selectedTransactionForDetail = it },
                        onAddTransactionClick = { showAddDialog = true },
                        onSimulateScenario = { scenario -> auditViewModel.simulateLiveTransaction(scenario) }
                    )
                }
                2 -> {
                    ReportScreen(
                        transactions = transactions,
                        csvContent = auditViewModel.getExportCsvText(),
                        onPushToGitHubClick = { currentTabIndex = 3 }
                    )
                }
                3 -> {
                    GitHubSyncScreen(
                        config = gitHubConfig,
                        isSyncing = isSyncing,
                        onSaveConfig = { auditViewModel.saveGitHubConfig(it) },
                        onSyncNow = { msg -> auditViewModel.syncGitHub(msg) },
                        onTestConnection = { callback -> auditViewModel.testGitHubConnection(callback) }
                    )
                }
                4 -> {
                    AuditorProfileScreen(
                        activeAuditor = activeAuditor,
                        auditLogs = auditLogs,
                        onSwitchAuditor = { auditViewModel.switchAuditor(it) }
                    )
                }
            }
        }
    }

    // Add Transaction Dialog
    if (showAddDialog) {
        val nextRrn = "RRN_%03d".format(transactions.size + 1)
        AddTransactionDialog(
            suggestedRrn = nextRrn,
            onDismiss = { showAddDialog = false },
            onSave = { rrn, waktu, idKasir, userIdKonsumen, nominal ->
                auditViewModel.addTransaction(rrn, waktu, idKasir, userIdKonsumen, nominal)
            }
        )
    }

    // Transaction Detail & Investigation Dialog
    selectedTransactionForDetail?.let { currentTx ->
        TransactionDetailDialog(
            transaction = currentTx,
            onDismiss = { selectedTransactionForDetail = null },
            onUpdateStatus = { status, notes ->
                auditViewModel.updateInvestigationStatus(currentTx.rrn, status, notes)
            },
            onDelete = {
                auditViewModel.deleteTransaction(currentTx)
            }
        )
    }

    // Real-Time Notification & Alert BottomSheet
    if (showAlertsSheet) {
        RealtimeAlertsSheet(
            alerts = realtimeAlerts,
            onDismiss = { showAlertsSheet = false }
        )
    }
}
