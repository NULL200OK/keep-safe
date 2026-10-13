package com.nabilhassan.keepsafe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ScanUiState {
    object Idle : ScanUiState()
    object Loading : ScanUiState()
    data class Success(val report: UrlReport) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
}

class ScanViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val state: StateFlow<ScanUiState> = _state

    fun scan(url: String) {
        if (url.isBlank()) {
            _state.value = ScanUiState.Error("أدخل رابطًا | Enter URL")
            return
        }
        _state.value = ScanUiState.Loading
        viewModelScope.launch {
            try {
                val report = withContext(Dispatchers.IO) {
                    UrlSecurityChecker.analyze(
                        url, ThreatDatabase.getInstance(getApplication()))
                }
                _state.value = ScanUiState.Success(report)
                withContext(Dispatchers.IO) {
                    ThreatDatabase.getInstance(getApplication())
                        .scanHistoryDao().insert(ScanHistory(
                            url = report.originalUrl,
                            host = report.host,
                            score = report.risk.score,
                            verdictAr = report.risk.verdictAr,
                            verdictEn = report.risk.verdictEn
                        ))
                }
                if (report.risk.score >= 50) {
                    SmartNotificationManager.notifyRisk(
                        getApplication(), report.host,
                        report.risk.verdictAr, report.risk.verdictEn, report.risk.score)
                }
            } catch (e: Exception) {
                _state.value = ScanUiState.Error(e.message ?: "Error")
            }
        }
    }

    fun reset() { _state.value = ScanUiState.Idle }
}