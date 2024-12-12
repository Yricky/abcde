package me.yricky.abcde.desktop.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import me.yricky.abcde.desktop.DesktopUtils.dataDir
import me.yricky.abcde.desktop.DesktopUtils.json
import java.awt.GraphicsEnvironment
import java.io.File

@Serializable
data class HistoryConfig(
    @SerialName("openedFile")
    val openedFile: List<OpenedFile> = emptyList()
){
    @Serializable
    class OpenedFile(
        @SerialName("lastTime")
        val lastTime: Long,
        @SerialName("path")
        val path: String
    )
    companion object{
        suspend fun edit(action: (HistoryConfig) -> HistoryConfig){
            withContext(Dispatchers.IO){
                val appConfig = action(inst.value)
                file.writeText(json.encodeToString(appConfig))
                inst.value = appConfig
            }
        }


        private val file = File(dataDir,"history.json")
        private val inst = MutableStateFlow(kotlin.runCatching {
            json.decodeFromString<HistoryConfig>(file.readText())
        }.onFailure {
            it.printStackTrace()
        }.getOrNull() ?: HistoryConfig().also {
            file.writeText(json.encodeToString(it))
        })
        val flow :StateFlow<HistoryConfig> = inst.asStateFlow()
    }
}