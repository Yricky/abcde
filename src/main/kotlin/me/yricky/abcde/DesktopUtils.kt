package me.yricky.abcde

import java.awt.Desktop
import java.net.URI
import java.util.*

object DesktopUtils {
    private val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    val isLinux = os.contains("linux")
    val isWindows = os.contains("win")
    val isMacos = os.contains("macos")
    private val desktop by lazy{
        Desktop.getDesktop()
    }

    fun openUrl(url:String){
        kotlin.runCatching {
            desktop.browse(URI(url))
        }.onFailure {t ->
            System.err.println("error:${t.stackTraceToString()}")
            println("System:${os}")
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

    fun chatToMe(){
    }
}