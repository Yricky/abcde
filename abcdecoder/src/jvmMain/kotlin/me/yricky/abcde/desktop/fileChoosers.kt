package me.yricky.abcde.desktop

import me.yricky.abcde.util.SelectedAbcFile
import me.yricky.abcde.util.SelectedHapFile
import me.yricky.abcde.util.SelectedIndexFile
import java.io.File
import javax.swing.filechooser.FileFilter

val abcFileChooser = object : FileFilter() {
    override fun accept(pathname: File?): Boolean {
        return pathname?.extension?.uppercase() == SelectedAbcFile.EXT ||
                pathname?.extension?.uppercase() == SelectedIndexFile.EXT ||
                pathname?.extension?.uppercase() == SelectedHapFile.EXT ||
                (pathname?.isDirectory == true)
    }

    override fun getDescription(): String {
        return "OpenHarmony字节码、资源或应用包(*.abc,*.index,*.hap)"
    }
}
