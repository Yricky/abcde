package me.yricky.abcde.desktop

import me.yricky.abcde.util.SelectedAbcFile
import me.yricky.abcde.util.SelectedHapFile
import me.yricky.abcde.util.SelectedIndexFile
import java.io.File
import javax.swing.filechooser.FileFilter

val abcFileChooser = object : FileFilter() {
    override fun accept(pathname: File?): Boolean {
        return pathname?.extension?.uppercase() == SelectedAbcFile.EXT || (pathname?.isDirectory == true)
    }

    override fun getDescription(): String {
        return "OpenHarmony字节码文件(*.abc)"
    }
}

val resIndexFileChooser = object : FileFilter() {
    override fun accept(pathname: File?): Boolean {
        return pathname?.extension?.uppercase() == SelectedIndexFile.EXT || (pathname?.isDirectory == true)
    }

    override fun getDescription(): String {
        return "OpenHarmony资源索引文件(*.index)"
    }
}

val hapFileChooser = object : FileFilter() {
    override fun accept(pathname: File?): Boolean {
        return pathname?.extension?.uppercase() == SelectedHapFile.EXT || (pathname?.isDirectory == true)
    }

    override fun getDescription(): String {
        return "OpenHarmony应用包文件(*.hap)"
    }
}

