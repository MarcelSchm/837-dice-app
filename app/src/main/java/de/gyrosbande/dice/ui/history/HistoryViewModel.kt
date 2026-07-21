package de.gyrosbande.dice.ui.history

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.graphics.Bitmap
import de.gyrosbande.dice.DiceApp
import de.gyrosbande.dice.data.transfer.HistoryExport
import de.gyrosbande.dice.data.transfer.MergeReport
import de.gyrosbande.dice.domain.HistoryRound
import de.gyrosbande.dice.domain.StatsCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryViewModel(private val app: DiceApp) : ViewModel() {

    val rounds: StateFlow<List<HistoryRound>> = app.historyRepository.observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Selected festival year; null = all years. */
    var selectedYear by mutableStateOf<Int?>(null)

    /** Result of the last import, shown in a dialog. */
    var importReport by mutableStateOf<MergeReport?>(null)
        private set

    /** User-facing error for a failed import/export. */
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun filtered(all: List<HistoryRound>): List<HistoryRound> =
        selectedYear?.let { year -> all.filter { it.year == year } } ?: all

    fun exportFileName(): String {
        val stamp = SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY).format(Date())
        return "837-dice-historie-$stamp.json"
    }

    /**
     * Renders the hall of fame for [rounds] as a PNG and hands a shareable
     * Uri to [onReady]. [subtitle] names the period (year or "gesamt").
     */
    fun shareStatsImage(rounds: List<HistoryRound>, subtitle: String, onReady: (Uri) -> Unit) {
        viewModelScope.launch {
            try {
                val uri = withContext(Dispatchers.IO) {
                    val stats = StatsCalculator.calculate(rounds)
                    val bitmap = StatsImage.render(app, stats, subtitle)
                    val dir = File(app.cacheDir, "exports").apply { mkdirs() }
                    val file = File(dir, "837-dice-hall-of-fame.png")
                    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    bitmap.recycle()
                    FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
                }
                onReady(uri)
            } catch (e: Exception) {
                errorMessage = "Bild teilen fehlgeschlagen: ${e.message}"
            }
        }
    }

    /** Writes the export into the cache dir and hands a shareable Uri to [onReady]. */
    fun exportForSharing(onReady: (Uri) -> Unit) {
        viewModelScope.launch {
            try {
                val uri = withContext(Dispatchers.IO) {
                    val dir = File(app.cacheDir, "exports").apply { mkdirs() }
                    val file = File(dir, exportFileName())
                    file.writeText(buildJson())
                    FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
                }
                onReady(uri)
            } catch (e: Exception) {
                errorMessage = "Export fehlgeschlagen: ${e.message}"
            }
        }
    }

    /** Writes the export to a user-picked location (SAF). */
    fun exportTo(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = buildJson()
                app.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray(Charsets.UTF_8))
                } ?: error("Datei konnte nicht geöffnet werden")
            } catch (e: Exception) {
                errorMessage = "Export fehlgeschlagen: ${e.message}"
            }
        }
    }

    /** Reads a shared export file and merges it into the local history. */
    fun importFrom(uri: Uri) {
        viewModelScope.launch {
            try {
                val report = withContext(Dispatchers.IO) {
                    val text = app.contentResolver.openInputStream(uri)
                        ?.use { it.readBytes().toString(Charsets.UTF_8) }
                        ?: error("Datei konnte nicht gelesen werden")
                    app.historyRepository.import(HistoryExport.fromJson(text))
                }
                importReport = report
            } catch (e: Exception) {
                errorMessage = "Import fehlgeschlagen. Ist das eine 837-Dice-Exportdatei?"
            }
        }
    }

    fun deleteRound(uuid: String) {
        viewModelScope.launch { app.historyRepository.deleteRound(uuid) }
    }

    fun dismissDialogs() {
        importReport = null
        errorMessage = null
    }

    private suspend fun buildJson(): String {
        val appVersion = app.packageManager.getPackageInfo(app.packageName, 0).versionName ?: "?"
        return HistoryExport.toJson(app.historyRepository.buildExport(appVersion))
    }
}
