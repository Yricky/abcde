package me.yricky.abcde

import androidx.compose.runtime.*
import me.yricky.abcde.page.AbcOverview
import me.yricky.abcde.page.ClassView
import me.yricky.abcde.page.CodeView
import me.yricky.abcde.page.Page
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.cfm.AbcClass

class AppState {


    val pageStack = mutableStateListOf<Page>()

    var currPage:Page? by mutableStateOf(null)

    fun openHap(){

    }

    fun openResIndex(){

    }

    fun openAbc(abc: me.yricky.oh.abcd.AbcBuf){
        AbcOverview(abc).also {
            currPage = it
            if(!pageStack.contains(it)){
                pageStack.add(it)
            }
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