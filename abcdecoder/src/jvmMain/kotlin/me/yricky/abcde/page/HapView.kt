package me.yricky.abcde.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import me.yricky.abcde.AppState
import me.yricky.abcde.desktop.DesktopUtils
import me.yricky.abcde.ui.Icons
import me.yricky.abcde.ui.TreeItemList
import me.yricky.abcde.util.SelectedAbcFile
import me.yricky.abcde.util.SelectedIndexFile
import me.yricky.abcde.util.TreeModel
import me.yricky.abcde.util.TypedFile
import me.yricky.oh.common.TreeStruct
import me.yricky.oh.hapde.Constant.DIR_ETS
import me.yricky.oh.hapde.Constant.DIR_LIB
import me.yricky.oh.hapde.Constant.DIR_RES
import me.yricky.oh.hapde.Constant.ENTRY_MODULE_JSON
import me.yricky.oh.hapde.Constant.ENTRY_PACK_INFO
import me.yricky.oh.hapde.Constant.ENTRY_RES_INDEX
import me.yricky.oh.hapde.HapConfig
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.streams.asSequence

class HapView(private val hap:ZipFile):Page() {
    companion object{
        val json = Json { ignoreUnknownKeys = true }
    }

    override val navString: String = asNavString("HAP", hap.name)
    override val name: String = hap.name

    val tree by lazy {
        TreeModel(TreeStruct(hap.stream().asSequence().asIterable(), pathOf = { it.name }))
    }
    var list by mutableStateOf(tree.buildFlattenList())
        private set
    var filter by mutableStateOf("")
        private set
    fun isFilterMode() = filter.isNotEmpty()

    val config by lazy {
        json.decodeFromString(HapConfig.serializer(),hap.getInputStream(hap.getEntry(ENTRY_MODULE_JSON)).reader().readText())
    }

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

    private val entryCache = mutableMapOf<String,TypedFile>()
    private suspend inline fun <reified T:TypedFile> getEntryFile(entryName:String, crossinline getter:(File) -> T):T? = withContext(Dispatchers.IO){
        val ret = entryCache[entryName]
        val entry = tree.tree.pathMap[entryName]?.value ?: return@withContext null
        if(ret == null){
            val file = File.createTempFile(hap.name,entryName,DesktopUtils.tmpDir)
            file.deleteOnExit()
            hap.getInputStream(entry).transferTo(file.outputStream())
            return@withContext getter(file).also {
                entryCache[entryName] = it
            }
        }
        return@withContext (ret as? T) ?: getter(ret.file).also {
            entryCache[entryName] = it
        }
    }
    private val thumbnailCache = mutableStateMapOf<String,Painter>()
    @Composable
    private fun loadPainterInZip(entryName:String):Painter {
        val node = tree.tree.pathMap[entryName] ?: return Icons.image()
        val density = LocalDensity.current
        return thumbnailCache[node.value.name] ?: produceState(Icons.image()) {
            withContext(Dispatchers.IO + NonCancellable) {
                val cache = thumbnailCache[node.value.name]
                if(cache != null) { value = cache } else kotlin.runCatching {
                    hap.getInputStream(node.value).use {
                        if(entryName.endsWith(".svg")){
                            loadSvgPainter(it,density)
                        }else BitmapPainter(loadImageBitmap(it))
                    }
                }.onFailure {
                    println("load failed: ${node.value.name}")
                    it.printStackTrace()
                }.onSuccess {
                    thumbnailCache[node.value.name] = it
                    value = it
                }
            }
        }.value
    }

    @Composable
    override fun Page(modifier: Modifier, appState: AppState) {
        Row {
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
                            appState.coroutineScope.launch {
                                appState.openPage(AbcView(
                                    getEntryFile(it.value.name){ f ->SelectedAbcFile(f,it.value.name) }!!.abcBuf,
                                    this@HapView
                                ))
                            }
                        } else if(it.pathSeg == ENTRY_RES_INDEX){
                            appState.coroutineScope.launch {
                                appState.openPage(ResIndexView(
                                    getEntryFile(it.value.name){ f -> SelectedIndexFile(f,it.value.name) }!!.resBuf,
                                    it.value.name,
                                    this@HapView
                                ))
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
            if(DesktopUtils.enableExpFeat) Column(Modifier.width(320.dp).padding(16.dp).verticalScroll(
                rememberScrollState()
            )) {
                val iconEntry by produceState<String?>(null){
                    withContext(Dispatchers.IO + NonCancellable){
                        println("icon:${config.app.icon.fileName}")
                        value = getEntryFile(ENTRY_RES_INDEX){ f -> SelectedIndexFile(f, ENTRY_RES_INDEX) }
                            ?.resBuf?.resMap?.entries
                            ?.firstOrNull { it.value.any { it.fileName == config.app.icon.fileName } }?.value
                            ?.firstOrNull()?.data?.takeIf {
                                println(it)
                                it.startsWith("${config.module.name}/")
                            }
                            ?.removePrefix("${config.module.name}/").also {
                                println("iconEntry:${it}")
                            }
                    }
                }
                Image(iconEntry?.let { loadPainterInZip(it) } ?: Icons.image(),null,
                    modifier = Modifier.align(Alignment.CenterHorizontally).size(80.dp)
                )
                val name by produceState(config.app.label.indexStr){
                    println("label:${config.app.label.fileName}")
                    value = getEntryFile(ENTRY_RES_INDEX){ f -> SelectedIndexFile(f, ENTRY_RES_INDEX) }
                        ?.resBuf?.resMap?.entries
                        ?.firstOrNull { it.value.any { it.fileName == config.app.label.fileName } }?.value
                        ?.firstOrNull()?.data ?: config.app.label.indexStr
                }
                Text("名称:${name}")
                Text("模块名:${config.module.name}")
            }
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
                                            node.value.name.endsWith(".gif") ||
                                            node.value.name.endsWith(".svg")
                                    ) && !node.value.isDirectory) {
                            loadPainterInZip(node.value.name)
                        } else {
                            Icons.anyType()
                        }
                    }
                }
            }
        }
    }
}