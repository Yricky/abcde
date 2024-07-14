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

    private fun navPage(page: Page?){
        currPage = page
        println("route to ${page?.navString}")
    }

    fun open(file:SelectedFile){
        if(!file.valid()){
            return
        }
        when(file){
            is SelectedAbcFile -> AbcView(file.abcBuf).also {
                navPage(it)
                if(!pageStack.contains(it)){
                    pageStack.add(it)
                }
            }

            is SelectedHapFile -> HapView(file.hap.getOrThrow()).also{
                navPage(it)
                if(!pageStack.contains(it)){
                    pageStack.add(it)
                }
            }
            is SelectedIndexFile -> ResIndexView(file.resBuf, file.tag).also{
                navPage(it)
                if(!pageStack.contains(it)){
                    pageStack.add(it)
                }
            }
        }
    }

    fun openPage(page: Page){
        navPage(page)
        if(!pageStack.contains(page)){
            pageStack.add(page)
        }
    }

    fun openClass(page:HapView?,classItem: AbcClass){
        ClassView(classItem,page).also {
            navPage(it)
            if(!pageStack.contains(it)){
                pageStack.add(it)
            }
        }
    }

    fun openCode(page:HapView?,method: AbcMethod){
        method.codeItem?.let {
            CodeView(it,page).also {
                navPage(it)
                if(!pageStack.contains(it)){
                    pageStack.add(it)
                }
            }
        }

    }

    fun closePage(page: Page){
        val index = pageStack.indexOf(page)
        if(index >= 0){
            pageStack.removeAt(index)
            if(currPage == page){
                navPage(pageStack.getOrNull(index) ?: pageStack.lastOrNull())

            }
        }
    }

    fun gotoPage(page: Page){
        if(!pageStack.contains(page)){
            pageStack.add(page)
        }
        navPage(page)
    }


}