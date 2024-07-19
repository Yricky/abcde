package me.yricky.abcde

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.yricky.abcde.page.*
import me.yricky.abcde.util.SelectedAbcFile
import me.yricky.abcde.util.SelectedFile
import me.yricky.abcde.util.SelectedHapFile
import me.yricky.abcde.util.SelectedIndexFile

class AppState {
    val coroutineScope = CoroutineScope(Dispatchers.Default)

    val stubHapSession = HapSession(null)

    val hapSessions = mutableStateListOf<HapSession>()

    var currHapSession by mutableStateOf(stubHapSession)

    fun open(file:SelectedFile,hapSession: HapSession? = null){
        if(!file.valid()){
            return
        }
        val session = hapSession ?: stubHapSession
        when(file){
            is SelectedAbcFile -> AbcView(file.abcBuf).also {
                session.openPage(it)
            }
            is SelectedHapFile -> HapView(file.hap.getOrThrow()).also{
                currHapSession = hapSessions.firstOrNull { s -> s.hapView == it } ?: HapSession(it).also { s ->
                    hapSessions.add(s)
                }
            }
            is SelectedIndexFile -> ResIndexView(file.resBuf, file.tag).also{
                session.openPage(it)
            }
        }
    }
}