package me.yricky.abcde

import androidx.compose.ui.text.toLowerCase
import java.awt.Desktop
import java.net.URI
import java.util.*

object DesktopUtils {
    private val desktop by lazy{
        Desktop.getDesktop()
    }

    fun openUrl(url:String){
        kotlin.runCatching {
            desktop.browse(URI(url))
        }.onFailure {t ->
            System.err.println("error:${t.stackTraceToString()}")
            val os = System.getProperty("os.name").lowercase(Locale.getDefault())
            println("System:${os}")
            when{
                os.contains("win") -> "explorer"
                os.contains("linux") -> "xdg-open"
                os.contains("macos") -> "open"
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