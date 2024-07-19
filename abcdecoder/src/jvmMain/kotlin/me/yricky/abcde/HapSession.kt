package me.yricky.abcde

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import me.yricky.abcde.page.*
import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.cfm.AbcMethod

class HapSession(
    val hapView: HapView?
) {
    val pageStack = mutableStateListOf<AttachHapPage>()

    var currPage: Page? by mutableStateOf(hapView)

    private fun navPage(page: Page?){
        currPage = page
        println("route to ${page?.navString}")
    }

    fun openPage(page: AttachHapPage){
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

    fun closePage(page: AttachHapPage){
        val index = pageStack.indexOf(page)
        if(index >= 0){
            pageStack.removeAt(index)
            if(currPage == page){
                navPage(pageStack.getOrNull(index) ?: pageStack.lastOrNull())
            }
        }
    }

    fun gotoPage(page: Page){
        if(page is AttachHapPage){
            if(!pageStack.contains(page)){
                pageStack.add(page)
            }
            navPage(page)
        } else if(page == hapView){
            navPage(page)
        }
    }
}