package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuditRepository
import com.example.data.db.AuditLogEntity
import com.example.data.github.GitHubConfig
import com.example.data.github.SyncResult
import com.example.engine.ForensicAuditEngine
import com.example.model.AuditorProfile
import com.example.model.ForensicSummary
import com.example.model.QrisTransaction
import com.example.notification.RealtimeAlertItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class AuditViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuditRepository(application)

    // Base flows from repository
    val transactions: StateFlow<List<QrisTransaction>> = repository.auditedTransactionsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val summary: StateFlow<ForensicSummary> = transactions.combine(MutableStateFlow(Unit)) { txList, _ ->
        ForensicAuditEngine.calculateSummary(txList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ForensicSummary()
    )

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val realtimeAlerts: StateFlow<List<RealtimeAlertItem>> = repository.realtimeAlerts
    val gitHubConfig: StateFlow<GitHubConfig> = repository.gitHubConfig
    val activeAuditor: StateFlow<AuditorProfile> = repository.activeAuditor
    val isSyncing: StateFlow<Boolean> = repository.isSyncing

    // Filtering & Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedRiskFilter = MutableStateFlow("Semua")
    val selectedRiskFilter: StateFlow<String> = _selectedRiskFilter.asStateFlow()

    private val _selectedIndicatorFilter = MutableStateFlow<String?>(null)
    val selectedIndicatorFilter: StateFlow<String?> = _selectedIndicatorFilter.asStateFlow()

    // Filtered transaction list
    val filteredTransactions: StateFlow<List<QrisTransaction>> = combine(
        transactions,
        searchQuery,
        selectedRiskFilter,
        selectedIndicatorFilter
    ) { txList, query, riskFilter, indicatorFilter ->
        txList.filter { tx ->
            val matchesQuery = query.isBlank() ||
                tx.rrn.contains(query, ignoreCase = true) ||
                tx.idKasir.contains(query, ignoreCase = true) ||
                tx.userIdKonsumen.contains(query, ignoreCase = true) ||
                tx.nominal.toString().contains(query)

            val matchesRisk = when (riskFilter) {
                "Risiko Tinggi" -> tx.totalFraudScore >= 2
                "Risiko Rendah" -> tx.totalFraudScore == 1
                "Aman" -> tx.totalFraudScore == 0
                else -> true
            }

            val matchesIndicator = when (indicatorFilter) {
                "Jam Tutup" -> tx.flagJamTutup == 1
                "Split Tx" -> tx.flagSplitTx == 1
                "Nominal Bulat" -> tx.flagNominalBulat == 1
                "Self-Tx" -> tx.flagSelfTx == 1
                "Benford Anomali" -> tx.flagBenfordAnomali == 1
                else -> true
            }

            matchesQuery && matchesRisk && matchesIndicator
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI Feedback state
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setRiskFilter(filter: String) {
        _selectedRiskFilter.value = filter
    }

    fun setIndicatorFilter(indicator: String?) {
        _selectedIndicatorFilter.value = indicator
    }

    fun addTransaction(
        rrn: String,
        waktu: String,
        idKasir: String,
        userIdKonsumen: String,
        nominal: Long
    ) {
        viewModelScope.launch {
            val tx = QrisTransaction(
                rrn = rrn.trim(),
                waktu = waktu.trim(),
                idKasir = idKasir.trim().uppercase(),
                userIdKonsumen = userIdKonsumen.trim().uppercase(),
                nominal = nominal
            )
            repository.insertTransaction(tx)
            _userMessage.value = "Transaksi ${tx.rrn} berhasil ditambahkan dan diaudit real-time."
        }
    }

    fun updateTransaction(transaction: QrisTransaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            _userMessage.value = "Transaksi ${transaction.rrn} berhasil diperbarui."
        }
    }

    fun deleteTransaction(transaction: QrisTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            _userMessage.value = "Transaksi ${transaction.rrn} berhasil dihapus."
        }
    }

    fun updateInvestigationStatus(rrn: String, status: String, notes: String) {
        viewModelScope.launch {
            repository.updateInvestigation(rrn, status, notes)
            _userMessage.value = "Status investigasi $rrn diperbarui."
        }
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            repository.resetToDefaultSeed()
            _userMessage.value = "Data berhasil direset ke dataset contoh audit forensik."
        }
    }

    fun syncGitHub(customMessage: String? = null, onComplete: ((SyncResult) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.syncNowToGitHub(customMessage)
            _userMessage.value = result.message
            onComplete?.invoke(result)
        }
    }

    fun testGitHubConnection(onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val res = repository.testGitHubConnection()
            onResult(res)
        }
    }

    fun saveGitHubConfig(config: GitHubConfig) {
        repository.saveGitHubConfig(config)
        _userMessage.value = "Pengaturan sinkronisasi GitHub disimpan."
    }

    fun switchAuditor(auditor: AuditorProfile) {
        repository.switchAuditor(auditor)
        _userMessage.value = "Beralih ke auditor: ${auditor.name}"
    }

    /**
     * Generates a realistic simulated live QRIS transaction.
     * Can generate normal transactions or intentional anomaly scenarios for testing.
     */
    fun simulateLiveTransaction(scenario: Int = 0) {
        viewModelScope.launch {
            val currentList = transactions.value
            val nextIndex = currentList.size + 1
            val nextRrn = "RRN_%03d".format(nextIndex)
            val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

            val tx = when (scenario) {
                1 -> { // Anomaly: Jam tutup (midnight)
                    val lateHourStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) + " 23:40:00"
                    QrisTransaction(
                        rrn = nextRrn,
                        waktu = lateHourStr,
                        idKasir = "KASIR_02",
                        userIdKonsumen = "CUST_${Random.nextInt(10, 99)}",
                        nominal = 185000L
                    )
                }
                2 -> { // Anomaly: Split transaction (repeat last customer nominal)
                    val lastCust = currentList.lastOrNull()?.userIdKonsumen ?: "CUST_55"
                    val lastNominal = currentList.lastOrNull()?.nominal ?: 450000L
                    QrisTransaction(
                        rrn = nextRrn,
                        waktu = nowStr,
                        idKasir = "KASIR_01",
                        userIdKonsumen = lastCust,
                        nominal = lastNominal
                    )
                }
                3 -> { // Anomaly: Gestun / Round big nominal
                    val roundNominal = listOf(1_500_000L, 2_000_000L, 4_000_000L, 10_000_000L).random()
                    QrisTransaction(
                        rrn = nextRrn,
                        waktu = nowStr,
                        idKasir = "KASIR_03",
                        userIdKonsumen = "CUST_${Random.nextInt(10, 99)}",
                        nominal = roundNominal
                    )
                }
                4 -> { // Anomaly: Self-transaction / Cashier collusion
                    val kasir = listOf("KASIR_01", "KASIR_02", "KASIR_03").random()
                    QrisTransaction(
                        rrn = nextRrn,
                        waktu = nowStr,
                        idKasir = kasir,
                        userIdKonsumen = kasir,
                        nominal = 350000L
                    )
                }
                5 -> { // Anomaly: Benford law anomaly (Nominal > 500k starts with 8 or 9)
                    val benfordNominal = listOf(875000L, 940000L, 998000L, 780000L).random()
                    QrisTransaction(
                        rrn = nextRrn,
                        waktu = nowStr,
                        idKasir = "KASIR_01",
                        userIdKonsumen = "CUST_${Random.nextInt(10, 99)}",
                        nominal = benfordNominal
                    )
                }
                else -> { // Normal transaction
                    val normalNominal = (Random.nextInt(15, 250) * 1000L) + (Random.nextInt(1, 9) * 100L)
                    QrisTransaction(
                        rrn = nextRrn,
                        waktu = nowStr,
                        idKasir = "KASIR_0${Random.nextInt(1, 4)}",
                        userIdKonsumen = "CUST_${Random.nextInt(10, 99)}",
                        nominal = normalNominal
                    )
                }
            }
            repository.insertTransaction(tx)
            _userMessage.value = "Simulasi transaksi $nextRrn berhasil ditambahkan."
        }
    }

    fun getExportCsvText(): String {
        return ForensicAuditEngine.exportToCsv(transactions.value)
    }
}
