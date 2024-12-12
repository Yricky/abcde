package me.yricky.abcde.desktop

import dev.dirs.ProjectDirectories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.skiko.GraphicsApi
import java.awt.Desktop
import java.awt.GraphicsEnvironment
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.util.*

object DesktopUtils {
    val json = Json{
        encodeDefaults = true
    }
    private val projectFiles = ProjectDirectories.from("me","yricky","abcdecoder")
    private val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    val isLinux = os.contains("linux")
    val isWindows = os.contains("win")
    val isMacos = os.contains("mac os")

    val dataDir = File(projectFiles.dataDir).also {
        if(!it.exists()){
            it.mkdirs()
        }
    }
    val tmpDir:File = Files.createTempDirectory("abcdecoder").toFile()

    val properties:Map<String,String> by lazy {
        javaClass.classLoader.getResourceAsStream("generated/properties")
            ?.let { it.use { String(it.readAllBytes()) } }
            ?.let { json.decodeFromString<Map<String,String>>(it) }
            ?: emptyMap()
    }

    private val desktop by lazy{
        Desktop.getDesktop()
    }

    fun openUrl(url:String){
        kotlin.runCatching {
            desktop.browse(URI(url))
        }.onFailure { t ->
            System.err.println("error:${t.stackTraceToString()}")
            println("System:$os")
            when{
                isWindows -> "explorer"
                isLinux -> "xdg-open"
                isMacos -> "open"
                else -> null
            }?.let {
                println("cmd:$it")
                ProcessBuilder(it,url).start()
            }
        }
    }

    object AppStatus{
        var renderApi:GraphicsApi? = null
    }
}