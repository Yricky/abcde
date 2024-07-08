package me.yricky.abcde

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.yricky.abcde.page.*
import me.yricky.abcde.util.SelectedAbcFile
import me.yricky.abcde.util.SelectedFile
import me.yricky.abcde.util.SelectedHapFile
import me.yricky.abcde.util.SelectedIndexFile
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.cfm.AbcClass

class AppState {
    val coroutineScope = CoroutineScope(Dispatchers.Default)


    val pageStack = mutableStateListOf<Page>()

    var currPage:Page? by mutableStateOf(null)

    fun open(file:SelectedFile){
        if(!file.valid()){
            return
        }
        when(file){
            is SelectedAbcFile -> AbcView(file.abcBuf).also {
                currPage = it
                if(!pageStack.contains(it)){
                    pageStack.add(it)
                }
            }

            is SelectedHapFile -> HapView(file.hap.getOrThrow()).also{
                currPage = it
                if(!pageStack.contains(it)){
                    pageStack.add(it)
                }
            }
            is SelectedIndexFile -> TODO()
        }
    }

    fun openClass(classItem: AbcClass){
        ClassView(classItem).also {
            currPage = it
            if(!pageStack.contains(it)){
                pageStack.add(it)
            }
        }
    }

    fun openCode(method: AbcMethod){
        CodeView(method).also {
            currPage = it
            if(!pageStack.contains(it)){
                pageStack.add(it)
            }
        }
    }

    fun closePage(page: Page){
        val index = pageStack.indexOf(page)
        if(index >= 0){
            pageStack.removeAt(index)
            if(currPage == page){
                currPage = pageStack.getOrNull(index) ?: pageStack.lastOrNull()
            }
        }
    }

    fun gotoPage(page: Page){
        if(!pageStack.contains(page)){
            pageStack.add(page)
        }
        currPage = page
    }


}