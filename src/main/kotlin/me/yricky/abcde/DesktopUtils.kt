package me.yricky.abcde

import java.awt.Desktop
import java.net.URI

object DesktopUtils {
    private val desktop by lazy{
        Desktop.getDesktop()
    }

    fun openUrl(url:String){
        desktop.browse(URI(url))
    }
}