package me.yricky.abcde.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.yricky.abcde.AppState
import me.yricky.abcde.ui.Icons
import me.yricky.abcde.ui.TreeItemList
import me.yricky.abcde.util.SelectedAbcFile
import me.yricky.abcde.util.SelectedIndexFile
import me.yricky.abcde.util.TreeModel
import me.yricky.oh.common.TreeStruct
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.streams.asSequence

class HapView(val hap:ZipFile):Page() {
    override val tag: String = hap.name

    val tree by lazy {
        TreeModel(TreeStruct(hap.stream().asSequence().asIterable(), pathOf = { it.name }))
    }

    var list by mutableStateOf(tree.buildFlattenList())
        private set


    var filter by mutableStateOf("")
        private set
    fun isFilterMode() = filter.isNotEmpty()


    fun setNewFilter(str:String){
        filter = str
        if(!isFilterMode()){
            list = tree.buildFlattenList()
        } else {
            list = tree.buildFlattenList{ it.pathSeg.contains(filter) }
        }
    }

    fun toggleExpand(node: TreeStruct.TreeNode<ZipEntry>){
        if(!isFilterMode()){
            tree.toggleExpand(node)
            list = tree.buildFlattenList()
        }
    }

    private val entryCache = mutableMapOf<ZipEntry,File>()
    private val thumbnailCache = mutableMapOf<ZipEntry,Painter>()

    @Composable
    override fun Page(modifier: Modifier, appState: AppState) {
        TreeItemList(modifier,list,
            expand = { isFilterMode() || tree.isExpand(it) },
            applyContent = { content ->
                content()
                item {
                    Box(Modifier.fillMaxWidth().height(120.dp)){
                        Text("${tree.tree.pathMap.size}个文件",Modifier.align(Alignment.Center))
                    }
                }
            },
            onClick = {
            if (it is TreeStruct.LeafNode) {
                if(it.pathSeg.endsWith(".abc")){
                    appState.coroutineScope.launch(Dispatchers.IO) {
                        val file = entryCache[it.value] ?: File.createTempFile(hap.name,it.pathSeg).also { f ->
                            entryCache[it.value] = f
                        }
                        file.deleteOnExit()
                        hap.getInputStream(it.value).transferTo(file.outputStream())
                        appState.open(SelectedAbcFile(file,"${hap.name}${File.separator}${it.value}"))
                    }
                } else if(it.pathSeg == ENTRY_RES_INDEX){
                    appState.coroutineScope.launch(Dispatchers.IO) {
                        val file = entryCache[it.value] ?: File.createTempFile(hap.name,it.pathSeg).also { f ->
                            entryCache[it.value] = f
                        }
                        file.deleteOnExit()
                        hap.getInputStream(it.value).transferTo(file.outputStream())
                        appState.open(SelectedIndexFile(file,"${hap.name}${File.separator}${it.value}"))
                    }
                }
            } else if(it is TreeStruct.TreeNode){
                toggleExpand(it)
            }
        }) {
            when (val node = it) {
                is TreeStruct.LeafNode<ZipEntry> -> {
                    Image(iconOf(node), null, modifier = Modifier.padding(end = 2.dp).size(18.dp))
                }
                is TreeStruct.TreeNode<ZipEntry> -> {
                    Image(iconOf(node), null, modifier = Modifier.padding(end = 2.dp).size(18.dp))
                }
            }
            Text(it.pathSeg, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }

    @Composable
    fun iconOf(node:TreeStruct.Node<ZipEntry>):Painter{
        return when (node) {
            is TreeStruct.TreeNode<ZipEntry> -> {
                if(node.parent == tree.tree.rootNode){
                    when(node.pathSeg){
                        DIR_ETS -> Icons.moduleGroup()
                        DIR_LIB -> Icons.libraryFolder()
                        DIR_RES -> Icons.resourcesRoot()
                        else -> Icons.folder()
                    }
                } else {
                    Icons.folder()
                }
            }
            is TreeStruct.LeafNode<ZipEntry> -> {
                when(node.value.name){
                    ENTRY_MODULE_JSON -> Icons.info()
                    ENTRY_PACK_INFO -> Icons.info()
                    else -> {
                        if(node.pathSeg == ENTRY_RES_INDEX){
                            Icons.indexCluster()
                        } else if(node.value.name.endsWith(".so") && !node.value.isDirectory) {
                            Icons.library()
                        } else if(node.value.name.endsWith(".abc") && !node.value.isDirectory) {
                            Icons.listFiles()
                        } else if(node.value.name.endsWith(".json") && !node.value.isDirectory) {
                            Icons.json()
                        } else if((
                                    node.value.name.endsWith(".png") ||
                                            node.value.name.endsWith(".jpg") ||
                                            node.value.name.endsWith(".jpeg") ||
                                            node.value.name.endsWith(".webp") ||
                                            node.value.name.endsWith(".gif")
                                    ) && !node.value.isDirectory) {
                            thumbnailCache[node.value] ?: produceState(Icons.image()){
                                withContext(Dispatchers.IO){
                                    kotlin.runCatching {
                                        hap.getInputStream(node.value).use { BitmapPainter(loadImageBitmap(it)) }
                                    }.onFailure {
                                        println("load failed: ${node.value.name}")
                                        it.printStackTrace()
                                    }.onSuccess {
                                        thumbnailCache[node.value] = it
                                        value = it
                                    }
                                }
                            }.value
                        } else if(node.value.name.endsWith(".svg") && !node.value.isDirectory) {
                            val density = LocalDensity.current
                            thumbnailCache[node.value] ?: produceState(Icons.image()){
                                withContext(Dispatchers.IO){
                                    kotlin.runCatching {
                                        hap.getInputStream(node.value).use { loadSvgPainter(it,density) }
                                    }.onFailure {
                                        println("load failed: ${node.value.name}")
                                        it.printStackTrace()
                                    }.onSuccess {
                                        thumbnailCache[node.value] = it
                                        value = it
                                    }
                                }
                            }.value
                        } else {
                            Icons.anyType()
                        }
                    }
                }
            }
        }
    }

    companion object{
        const val ENTRY_RES_INDEX = "resources.index"
        const val ENTRY_MODULE_JSON = "module.json"
        const val ENTRY_PACK_INFO = "pack.info"
        const val DIR_ETS = "ets"
        const val DIR_LIB = "libs"
        const val DIR_RES = "resources"
    }
}