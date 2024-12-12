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
data class AppCommonConfig(
    @SerialName("density")
    val density:Float = GraphicsEnvironment.getLocalGraphicsEnvironment()
        ?.defaultScreenDevice
        ?.defaultConfiguration
        ?.defaultTransform
        ?.scaleX?.toFloat() ?: 1f,
    @SerialName("historyList")
    val historyList:List<String> = emptyList(),
    @SerialName("darkTheme")
    val darkTheme:Boolean? = true,
    @SerialName("futureFeature")
    val futureFeature:Boolean = false
){
    companion object{
        suspend fun edit(action: (AppCommonConfig) -> AppCommonConfig){
            withContext(Dispatchers.IO){
                println("edit!")
                val appConfig = action(inst.value)
                file.writeText(json.encodeToString(appConfig))
                inst.value = appConfig
            }
        }


        private val file = File(dataDir,"cfg.json")
        private val inst = MutableStateFlow(kotlin.runCatching {
            json.decodeFromString<AppCommonConfig>(file.readText())
        }.onFailure {
            it.printStackTrace()
        }.getOrNull() ?: AppCommonConfig().also {
            file.writeText(json.encodeToString(it))
        })
        val flow :StateFlow<AppCommonConfig> = inst.asStateFlow()
    }
}