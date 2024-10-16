package me.yricky.abcde.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.yricky.abcde.AppState
import me.yricky.abcde.HapSession
import me.yricky.abcde.content.AbcUniSearchState
import me.yricky.abcde.content.AbcUniSearchStateView
import me.yricky.abcde.ui.*
import me.yricky.abcde.util.TreeModel
import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.common.TreeStruct
import me.yricky.oh.abcd.cfm.ClassItem
import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.cfm.exportName
import me.yricky.oh.utils.Adler32

class AbcView(val abc: AbcBuf,override val hap:HapView? = null):AttachHapPage() {

    override val navString: String = "${hap?.navString ?: ""}${asNavString("ABC", abc.tag)}"
    override val name: String = if(hap == null){
        abc.tag
    } else "${hap.name}/${abc.tag}"

    @Composable
    override fun Page(modifier: Modifier, hapSession: HapSession, appState: AppState) {
        val scope = rememberCoroutineScope()
        val appCfg = LocalAppConfig.current
        VerticalTabAndContent(modifier, listOfNotNull(composeSelectContent{ _: Boolean ->
            Image(Icons.clazz(), null, Modifier.fillMaxSize(), colorFilter = grayColorFilter)
        } to composeContent{
            Column(Modifier.fillMaxSize().padding(end = 4.dp)) {
                OutlinedTextField(
                    value = filter,
                    onValueChange = { _filter ->
                        filter = _filter.replace(" ", "").replace("\n", "")
                        scope.launch {
                            if (classList.isNotEmpty()) {
                                delay(500)
                            }
                            if (_filter == filter) {
                                println("Set:$_filter")
                                setNewFilter(filter)
                            } else {
                                println("drop:${_filter}")
                            }
                        }
                    },
                    leadingIcon = {
                        Image(Icons.search(), null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("${classCount}个类")
                    },
                )
                TreeItemList(Modifier.fillMaxWidth().weight(1f), classList,
                    expand = { isFilterMode() || treeStruct.isExpand(it) },
                    onClick = {
                        if (it is TreeStruct.LeafNode) {
                            val clazz = it.value
                            if(clazz is AbcClass){
                                hapSession.openClass(hap,clazz)
                            }
                        } else if(it is TreeStruct.TreeNode){
                            toggleExpand(it)
                        }
                    }) {
                    when (val node = it) {
                        is TreeStruct.LeafNode<ClassItem> -> {
                            Image(node.value.icon(), null, modifier = Modifier.padding(end = 2.dp).size(20.dp))
                            val txt = remember(node.value) {
                                when(val clz = node.value){
                                    is AbcClass -> {
                                        val exportName = clz.exportName()
                                        if(exportName == null || exportName == it.pathSeg || exportName == "default"){
                                            it.pathSeg
                                        } else "${it.pathSeg} ($exportName)"
                                    }
                                    else -> {
                                        it.pathSeg
                                    }
                                }
                            }
                            Text(txt, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        is TreeStruct.TreeNode<ClassItem> -> {
                            Image(Icons.pkg(), null, modifier = Modifier.padding(end = 2.dp).size(20.dp))
                            Text(it.pathSeg, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }, composeSelectContent{
            Image(Icons.info(), null, Modifier.fillMaxSize(), colorFilter = grayColorFilter)
        } to composeContent{
            Column {
                Text(abc.tag, style = MaterialTheme.typography.titleLarge)
                Text("文件版本:${abc.header.version}")
                Text("size:${abc.header.fileSize}")
                Text("Class数量:${abc.header.numClasses}")
                Text("行号处理程序数量:${abc.header.numLnps}")
                Text("IndexRegion数量:${abc.header.numIndexRegions}")
                var realCkSum:Int? by remember { mutableStateOf(null) }
                LaunchedEffect(null){
                    if(realCheckSum.isInitialized()){
                        realCkSum = realCheckSum.value
                    }
                }
                Text("校验和:${String.format("%08X",abc.header.checkSum)}(${
                    when(realCkSum){
                        null -> "点击校验"
                        abc.header.checkSum -> "校验通过"
                        else -> "校验不通过，实际为${String.format("%08X",realCkSum ?: 0)}"
                    }
                })",Modifier.clickable {
                    scope.launch(Dispatchers.Default) { realCkSum = realCheckSum.value }
                })
            }
        }, composeSelectContent {
                Image(Icons.search(), null, Modifier.fillMaxSize(), colorFilter = grayColorFilter)
            } to composeContent {
                AbcUniSearchStateView(hapSession, this, searchState)
            }
        ))
    }

    val searchState = AbcUniSearchState(abc, CoroutineScope(Dispatchers.Default))

    private val classMap get()= abc.classes
    var filter by mutableStateOf("")
        private set
    val treeStruct = TreeModel(TreeStruct(classMap.values, pathOf = { it.name }))
    var classList by mutableStateOf(treeStruct.buildFlattenList())
        private set

    fun isFilterMode() = filter.isNotEmpty()

    var classCount by mutableStateOf(classMap.size)

    val realCheckSum = lazy {
        Adler32().apply {
            update(abc.buf.slice(12,abc.buf.limit() - 12))
        }.value()
    }

    fun setNewFilter(str:String){
        filter = str
        if(!isFilterMode()){
            classList = treeStruct.buildFlattenList()
        } else {
            classList = treeStruct.buildFlattenList{
                it.pathSeg.contains(filter) ||
                        ((it as? TreeStruct.LeafNode<ClassItem>)?.value as? AbcClass)?.exportName()?.contains(filter) == true
            }
        }
        classCount = if (isFilterMode()) classList.count { it.second is TreeStruct.LeafNode } else classMap.size
    }

    fun toggleExpand(node: TreeStruct.TreeNode<ClassItem>){
        if(!isFilterMode()){
            treeStruct.toggleExpand(node)
            classCount = classMap.size
            classList = treeStruct.buildFlattenList()
        }
    }
}