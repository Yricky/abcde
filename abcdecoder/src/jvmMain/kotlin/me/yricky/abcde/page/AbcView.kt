package me.yricky.abcde.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.yricky.abcde.AppState
import me.yricky.abcde.ui.*
import me.yricky.abcde.util.TreeModel
import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.common.TreeStruct
import me.yricky.oh.abcd.cfm.ClassItem
import me.yricky.oh.abcd.cfm.AbcClass
import java.util.zip.ZipFile

class AbcView(val abc: AbcBuf,override var hap:HapView? = null):AttachHapPage() {

    override val navString: String = "${hap?.navString ?: ""}${asNavString("ABC", abc.tag)}"
    override val name: String = if(hap == null){
        abc.tag
    } else "${hap?.name ?: ""}/${abc.tag}"

    @Composable
    override fun Page(modifier: Modifier, appState: AppState) {
        AbcViewPage(modifier, appState, this)
    }

    private val classMap get()= abc.classes
    var filter by mutableStateOf("")
        private set
    val treeStruct = TreeModel(TreeStruct(classMap.values, pathOf = { it.name }))
    var classList by mutableStateOf(treeStruct.buildFlattenList())
        private set

    fun isFilterMode() = filter.isNotEmpty()

    var classCount by mutableStateOf(classMap.size)

    fun setNewFilter(str:String){
        filter = str
        if(!isFilterMode()){
            classList = treeStruct.buildFlattenList()
        } else {
            classList = treeStruct.buildFlattenList{ it.pathSeg.contains(filter) }
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



@Composable
fun AbcViewPage(
    modifier: Modifier,
    appState: AppState,
    abcView: AbcView
) {

    val scope = rememberCoroutineScope()
    VerticalTabAndContent(modifier, listOf(composeSelectContent{ _: Boolean ->
        Image(Icons.clazz(), null, Modifier.fillMaxSize(), colorFilter = grayColorFilter)
    } to composeContent{
        Column(Modifier.fillMaxSize().padding(end = 4.dp)) {
            var filter by remember(abcView.filter) {
                mutableStateOf(abcView.filter)
            }
            OutlinedTextField(
                value = filter,
                onValueChange = { _filter ->
                    filter = _filter.replace(" ", "").replace("\n", "")
                    scope.launch {
                        if (abcView.classList.isNotEmpty()) {
                            delay(500)
                        }
                        if (_filter == filter) {
                            println("Set:$_filter")
                            abcView.setNewFilter(filter)
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
                    Text("${abcView.classCount}个类")
                },
            )
            TreeItemList(Modifier.fillMaxWidth().weight(1f), abcView.classList,
                expand = { abcView.isFilterMode() || abcView.treeStruct.isExpand(it) },
                onClick = {
                    if (it is TreeStruct.LeafNode) {
                        val clazz = it.value
                        if(clazz is AbcClass){
                            appState.openClass(abcView.hap,clazz)
                        }
                    } else if(it is TreeStruct.TreeNode){
                        abcView.toggleExpand(it)
                    }
                }) {
                when (val node = it) {
                    is TreeStruct.LeafNode<ClassItem> -> {
                        Image(node.value.icon(), null, modifier = Modifier.padding(end = 2.dp).size(20.dp))
                    }
                    is TreeStruct.TreeNode<ClassItem> -> {
                        Image(Icons.pkg(), null, modifier = Modifier.padding(end = 2.dp).size(20.dp))
                    }
                }
                Text(it.pathSeg, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }, composeSelectContent{
        Image(Icons.info(), null, Modifier.fillMaxSize(), colorFilter = grayColorFilter)
    } to composeContent{
        Column {
            Text(abcView.abc.tag, style = MaterialTheme.typography.titleLarge)
            Text("文件版本:${abcView.abc.header.version}")
            Text("size:${abcView.abc.header.fileSize}")
            Text("Class数量:${abcView.abc.header.numClasses}")
            Text("行号处理程序数量:${abcView.abc.header.numLnps}")
            Text("IndexRegion数量:${abcView.abc.header.numIndexRegions}")
        }
    }
    ))
}