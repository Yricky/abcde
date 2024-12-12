package me.yricky.abcde

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.yricky.abcde.desktop.config.AppCommonConfig
import me.yricky.abcde.desktop.config.HistoryConfig
import me.yricky.abcde.page.*
import me.yricky.abcde.util.SelectedAbcFile
import me.yricky.abcde.util.SelectedFile
import me.yricky.abcde.util.SelectedHapFile
import me.yricky.abcde.util.SelectedIndexFile
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile

val LocalAppCommonConfig = compositionLocalOf<AppCommonConfig> { error("No default value") }

class AppState {
    val coroutineScope = CoroutineScope(Dispatchers.Default)

    val stubHapSession = HapSession(null)

    val hapSessions = mutableStateListOf<HapSession>()

    var currHapSession by mutableStateOf(stubHapSession)

    var showSettings by mutableStateOf(false)

    fun open(file:SelectedFile,session: HapSession = stubHapSession){
        if(!file.valid()){
            return
        }
        var opened = false
        when(file){
            is SelectedAbcFile -> AbcView(file.abcBuf).also {
                session.openPage(it)
                currHapSession = session
                opened = true
            }
            is SelectedHapFile -> HapView(file).also{
                currHapSession = hapSessions.firstOrNull { s -> s.hapView == it } ?: HapSession(it).also { s ->
                    hapSessions.add(s)
                }
                opened = true
            }
            is SelectedIndexFile -> ResIndexView(file.resBuf, file.tag).also{
                session.openPage(it)
                currHapSession = session
                opened = true
            }
        }
        if(opened){
            coroutineScope.launch(Dispatchers.IO){
                HistoryConfig.edit { config ->
                    val history = config.openedFile.toMutableList()
                    history.removeIf { t ->
                        t.path == file.file.absolutePath || !Path(file.file.absolutePath).isRegularFile()
                    }
                    history.add(HistoryConfig.OpenedFile(
                        System.currentTimeMillis(),
                        file.file.absolutePath
                    ))
                    config.copy(openedFile = history)
                }
            }
        }
    }

    fun closeHap(hapSession: HapSession){
        if(currHapSession == hapSession){
            currHapSession = stubHapSession
        }
        hapSessions.remove(hapSession)
    }
}