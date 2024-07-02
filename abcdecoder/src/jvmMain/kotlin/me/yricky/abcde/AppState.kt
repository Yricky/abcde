package me.yricky.abcde

import androidx.compose.runtime.*
import me.yricky.abcde.util.TreeModel
import me.yricky.oh.common.TreeStruct
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.cfm.ClassItem
import me.yricky.oh.abcd.code.Code

class AppState() {


    val pageStack = mutableStateListOf<Page>()

    var currPage:Page? by mutableStateOf(null)

    fun openHap(){

    }

    fun openResIndex(){

    }

    fun openAbc(abc: me.yricky.oh.abcd.AbcBuf){
        ClassList(abc).also {
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

    sealed class Page{
        abstract val tag:String
    }

    class ClassList(val abc: me.yricky.oh.abcd.AbcBuf):Page() {
        override val tag: String = abc.tag

        private val classMap get()= abc.classes
        var filter by mutableStateOf("")
            private set
        private val treeStruct = TreeModel(TreeStruct(classMap.values, pathOf = { it.name }))
        var classList by mutableStateOf(treeStruct.buildFlattenList())
            private set

        fun isFilterMode() = filter.isNotEmpty()

        var classCount by mutableStateOf(classMap.size)

        fun setNewFilter(str:String){
            filter = str
            classList = treeStruct.buildFlattenList{ it.pathSeg.contains(filter) }
            classCount = if (isFilterMode()) classList.count { it.second is TreeStruct.LeafNode } else classMap.size
        }

        fun toggleExpand(node: TreeStruct.TreeNode<ClassItem>){
            if(!isFilterMode()){
                treeStruct.toggleExpand(node)
                classCount = classMap.size
                classList = treeStruct.buildFlattenList()
            }
        }

        override fun equals(other: Any?): Boolean {
            if(other !is ClassList){
                return false
            }
            return abc == other.abc
        }

        override fun hashCode(): Int {
            return abc.hashCode()
        }
    }

    class ClassView(val classItem: AbcClass):Page() {
        override val tag: String = classItem.name

        override fun equals(other: Any?): Boolean {
            if(other !is ClassView){
                return false
            }
            return classItem == other.classItem
        }

        override fun hashCode(): Int {
            return classItem.hashCode()
        }
    }

    class CodeView(val method: AbcMethod,val code: Code? = method.codeItem):Page() {
        override val tag: String = method.name

        override fun equals(other: Any?): Boolean {
            if(other !is CodeView){
                return false
            }
            return method == other.method
        }

        override fun hashCode(): Int {
            return method.hashCode()
        }
    }
}