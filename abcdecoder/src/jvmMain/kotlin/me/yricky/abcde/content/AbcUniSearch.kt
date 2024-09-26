package me.yricky.abcde.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.yricky.abcde.HapSession
import me.yricky.abcde.page.AbcView
import me.yricky.abcde.ui.Icons
import me.yricky.abcde.ui.LazyColumnWithScrollBar
import me.yricky.abcde.ui.codeStyle
import me.yricky.abcde.ui.defineStr
import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.cfm.ClassItem
import me.yricky.oh.abcd.isa.calledStrings

class AbcUniSearchState(
    val abc:AbcBuf,
    val searchScope:CoroutineScope
){
    var filterText by mutableStateOf("")


    class SearchSession(
        val job:Job,
        val filterText:String,
        val searchTargets:Map<SearchTarget,SnapshotStateList<SearchResult>>,
        val progress:State<Float>,
        val finished:State<Boolean>
    ){
    }

    fun startSearch(
        target: Set<SearchTarget>
    ):SearchSession{
        val progress = mutableFloatStateOf(0f)
        val targets = target.associateWith { mutableStateListOf<SearchResult>() }
        val finished = mutableStateOf(false)
        val job = searchScope.launch(Dispatchers.Default) {
            abc.classes.values.forEachIndexed { i,c ->
                targets.entries.forEach {
                    it.value.addAll(it.key.find(filterText,c))
                }
                progress.value = i / abc.classes.size.toFloat()
            }
            finished.value = true
        }
        return SearchSession(job,filterText,targets,progress,finished)
    }

    var session by mutableStateOf<AbcUniSearchState.SearchSession?>(null)

    sealed class SearchTarget{
        abstract fun find(filterText:String,classItem: ClassItem):List<SearchResult>
    }
    object ClassPath:SearchTarget(){
        override fun find(filterText:String,classItem: ClassItem): List<SearchResult> {
            return if(classItem.name.contains(filterText)){
                listOf(ClassResult(classItem))
            } else {
                emptyList()
            }
        }

    }
    object MethodName:SearchTarget(){
        override fun find(filterText: String, classItem: ClassItem): List<SearchResult> {
            return when(classItem){
                is AbcClass -> classItem.methods.filter { it.name.contains(filterText) }.map { MethodResult(classItem,it) }
                else -> emptyList()
            }
        }

    }
    object ASM:SearchTarget(){
        override fun find(filterText: String, classItem: ClassItem): List<SearchResult> {
            return when(classItem){
                is AbcClass -> classItem.methods.filter {
                    it.codeItem?.asm?.list?.any {
                        it.calledStrings.any { it.contains(filterText) }
                    } ?: false
                }.map { CodeResult(classItem,it) }
                else -> emptyList()
            }
        }
    }

    sealed class SearchResult()
    class ClassResult(val classItem: ClassItem):SearchResult()
    class MethodResult(val classItem: AbcClass,val method: AbcMethod):SearchResult()
    class CodeResult(val classItem: AbcClass,val method: AbcMethod):SearchResult()
}

@Composable
fun AbcUniSearchStateView(
    hapSession: HapSession,
    abc:AbcView,
    state:AbcUniSearchState
){
    Column {
        Row(
            Modifier.fillMaxWidth().height(40.dp)
                .border(1.dp,MaterialTheme.colorScheme.surfaceVariant),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                state.filterText,{state.filterText = it},
                textStyle = codeStyle,
                singleLine = true,
                maxLines = 1,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSecondaryContainer),
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                state.session?.job?.cancel()
                state.session = state.startSearch(setOf(AbcUniSearchState.MethodName,AbcUniSearchState.ASM))
            }, enabled = state.filterText.isNotBlank()){
                Text("查询")
            }
        }

        state.session?.let {
            AnimatedVisibility(!it.finished.value) {
                LinearProgressIndicator(progress = { it.progress.value }, modifier = Modifier.fillMaxWidth())
            }
            var tabIndex by remember { mutableStateOf(0) }
            TabRow(
                selectedTabIndex = tabIndex,
            ){
                Tab(
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 },
                    text = { Text("在方法名中查找") }
                )
                Tab(
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 },
                    text = { Text("在方法引用的字符串中查找") }
                )
            }
            LazyColumnWithScrollBar {
                it.searchTargets[when(tabIndex){
                    0 -> AbcUniSearchState.MethodName
                    1 -> AbcUniSearchState.ASM
                    else -> null
                }]?.let {  u ->
                    itemsIndexed(u){ _,it ->
                        when(it){
                            is AbcUniSearchState.ClassResult -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        if (it.classItem is AbcClass) {
                                            hapSession.openClass(abc.hap, it.classItem)
                                        }
                                    }
                                ) {
                                    Image(Icons.clazz(), null,modifier = Modifier.padding(8.dp))
                                    Text(it.classItem.name, modifier = Modifier.weight(1f))
                                }
                            }
                            is AbcUniSearchState.MethodResult -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        hapSession.openCode(abc.hap, it.method)
                                    }
                                ) {
                                    Image(Icons.method(),null,modifier = Modifier.padding(8.dp))
                                    Text(it.method.defineStr(true), modifier = Modifier.weight(1f))
                                }
                            }
                            is AbcUniSearchState.CodeResult -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        hapSession.openCode(abc.hap,it.method)
                                    }
                                ) {
                                    Image(Icons.asm(),null,modifier = Modifier.padding(8.dp))
                                    Text(it.method.defineStr(true), modifier = Modifier.weight(1f))
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}