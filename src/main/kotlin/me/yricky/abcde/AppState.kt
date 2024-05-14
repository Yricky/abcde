package me.yricky.abcde

import androidx.compose.runtime.*
import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.code.Code

class AppState(val abc:AbcBuf) {


    val pageStack = mutableStateListOf<Page>()

    val mainPage = ClassList(abc)
    val currPage get() = pageStack.lastOrNull() ?: mainPage

    fun openClass(classItem: AbcClass){
        pageStack.add(ClassView(classItem))
    }

    fun openCode(method: AbcMethod){
        pageStack.add(CodeView(method,method.codeItem))
    }

    fun clearPage(){
        pageStack.clear()
    }

    fun gotoPage(page: Page){
        while (pageStack.isNotEmpty() && pageStack.lastOrNull() != page){
            pageStack.removeLast()
        }
    }

    sealed class Page{
        abstract val tag:String
    }

    class ClassList(val abc: AbcBuf):Page() {
        override val tag: String = "概览"

        val classMap get()= abc.classes
        var filter by mutableStateOf("")
        var classList by mutableStateOf(classMap.values.toList())
    }

    class ClassView(val classItem: AbcClass):Page() {
        override val tag: String = "类详情"
    }

    class CodeView(val method: AbcMethod,val code: Code?):Page() {
        override val tag: String = "方法详情"
    }
}