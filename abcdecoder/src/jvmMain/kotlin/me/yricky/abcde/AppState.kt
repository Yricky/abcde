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

    var showSettings by mutableStateOf(false)

    fun open(file:SelectedFile,session: HapSession = stubHapSession){
        if(!file.valid()){
            return
        }
        when(file){
            is SelectedAbcFile -> AbcView(file.abcBuf).also {
                session.openPage(it)
                currHapSession = session
            }
            is SelectedHapFile -> HapView(file).also{
                currHapSession = hapSessions.firstOrNull { s -> s.hapView == it } ?: HapSession(it).also { s ->
                    hapSessions.add(s)
                }
            }
            is SelectedIndexFile -> ResIndexView(file.resBuf, file.tag).also{
                session.openPage(it)
                currHapSession = session
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