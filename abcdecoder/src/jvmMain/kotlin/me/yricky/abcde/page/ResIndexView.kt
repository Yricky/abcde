package me.yricky.abcde.page

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import me.yricky.abcde.AppState
import me.yricky.abcde.HapSession
import me.yricky.abcde.content.ResItemCell
import me.yricky.abcde.desktop.DesktopUtils
import me.yricky.abcde.ui.LazyColumnWithScrollBar
import me.yricky.abcde.ui.SearchText
import me.yricky.abcde.ui.codeStyle
import me.yricky.oh.resde.ResIndexBuf
import me.yricky.oh.resde.ResType
import me.yricky.oh.resde.ResourceItem

class ResIndexView(val res:ResIndexBuf, name: String,override val hap:HapSession):AttachHapPage() {
    companion object{
        val emptyTable = ResTable()
    }
    private val scope = CoroutineScope(Dispatchers.Default)
    override val navString: String = "${hap.hapView?.navString ?: ""}${asNavString("REI", name)}"
    override val name: String = if(hap.hapView == null){ name } else "${hap.hapView.name}/$name"

    private val filterFlow = MutableStateFlow("")
    private val mapFlow: StateFlow<Map<ResType,ResTable>> = filterFlow.map { f ->
        val map = mutableMapOf<ResType,ResTable>()
        res.resMap.forEach { (id, u) ->
            u.forEach { item ->
                val table = map[item.resType] ?: ResTable().also {
                    map[item.resType] = it
                }
                if(item.fileName.contains(f) || item.data.contains(f)){
                    table.add(id,item)
                }
            }
        }
        println(map.keys.size)
        map
    }.flowOn(Dispatchers.Default).stateIn(scope, started = SharingStarted.Eagerly,emptyMap())

    class ResTable{
        val limitKeyConfigs = mutableListOf<String>()
        val table = mutableMapOf<Int,MutableList<ResTableItem>>()

        fun add(id: Int,item: ResourceItem){
            if(!limitKeyConfigs.contains(item.limitKey)){
                limitKeyConfigs.add(item.limitKey)
            }
            val list = table[id] ?: mutableListOf<ResTableItem>().also { table[id] = it }
            val tableItem = list.firstOrNull { it.name == item.fileName } ?: ResTableItem(item.fileName).also { list.add(it) }
            tableItem.data[item.limitKey] = item.data
        }
    }

    class ResTableItem(
        val name:String,
    ){
        val data = mutableMapOf<String,String>()
    }

    @Composable
    override fun Page(modifier: Modifier, hapSession: HapSession, appState: AppState) {
        Column {
            val filter by filterFlow.collectAsState()
            SearchText(filter,Modifier.padding(4.dp)){ filterFlow.value = it }
            val map:Map<ResType,ResTable> by mapFlow.collectAsState()
            val keys = remember(map){ map.keys.toList() }
            var currKey by remember(map) { mutableStateOf(keys.firstOrNull()) }
            CompositionLocalProvider(LocalTextStyle provides codeStyle){
                val ts = LocalTextStyle.current
                val lineHeight = with(LocalDensity.current){ (ts.fontSize * 1.3).toDp() }
                Row {
                    LazyColumnWithScrollBar(modifier = Modifier.width(160.dp)) {
                        items(keys){
                            Row {
                                if(currKey == it){
                                    Text("> ")
                                }
                                Text("${it}(${map[it]?.table?.size})",Modifier.fillMaxWidth().clickable { currKey = it })
                            }
                        }
                    }
                    Crossfade(currKey) { thisKey -> Column {
                        val table = map[thisKey] ?: emptyTable
                        val list = remember(currKey,map) { table.table.entries.toList() }
                        val hScrollState = rememberLazyListState()
                        var idHex by remember { mutableStateOf(false) }
                        Row(Modifier.background(MaterialTheme.colorScheme.background)) {
                            Text("id(${if (idHex) "HEX" else "DEC"})",
                                Modifier.width(160.dp).height(lineHeight).clickable{ idHex = !idHex },
                                maxLines = 1)
                            Text("name",Modifier.width(240.dp).height(lineHeight), maxLines = 1)
                            Box(Modifier.weight(1f)){
                                LazyRow(state = hScrollState){
                                    items(table.limitKeyConfigs){
                                        Text(it,Modifier.width(240.dp).height(lineHeight), maxLines = 1)
                                    }
                                    item{
                                        Spacer(Modifier.width(120.dp))
                                    }
                                }
                                HorizontalScrollbar(
                                    rememberScrollbarAdapter(hScrollState),
                                    Modifier.fillMaxWidth().align(Alignment.TopCenter)
                                )
                            }
                        }
                        val vScrollState = rememberLazyListState()
                        Box(modifier.pointerInput(PointerEventPass.Main) { awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                if(DesktopUtils.isMacos){
                                    event.changes.forEach {
                                        if(it.scrollDelta != Offset.Zero){
                                            hScrollState.dispatchRawDelta(it.scrollDelta.x * lineHeight.value)
                                            vScrollState.dispatchRawDelta(it.scrollDelta.y * lineHeight.value)
                                        }
                                    }
                                }
                            }
                        } }){
                            val scrollState = rememberLazyListState()
                                .also { it.requestScrollToItem(hScrollState.firstVisibleItemIndex,hScrollState.firstVisibleItemScrollOffset) }
                            LaunchedEffect(hScrollState.firstVisibleItemIndex,hScrollState.firstVisibleItemScrollOffset) {
                                scrollState.requestScrollToItem(hScrollState.firstVisibleItemIndex,hScrollState.firstVisibleItemScrollOffset)
                            }
                            LazyColumnWithScrollBar(Modifier.fillMaxSize(),
                                state = vScrollState,
                                userScrollEnabled = !DesktopUtils.isMacos
                            ) { items(list) { idItems ->
                                Column { idItems.value.forEach { item ->
                                    Row {
                                        SelectionContainer{
                                            Text(
                                                if(idHex) "0x${idItems.key.toString(16)}" else "${idItems.key}",
                                                Modifier.width(160.dp).height(lineHeight).border(0.5.dp,MaterialTheme.colorScheme.surfaceVariant),
                                                maxLines = 1
                                            )
                                        }
                                        Row {
                                            var namePop by remember { mutableStateOf(false) }
                                            Text(
                                                item.name,
                                                Modifier.width(240.dp).height(lineHeight).border(0.5.dp,MaterialTheme.colorScheme.surfaceVariant).clickable { namePop = !namePop },
                                                maxLines = 1, overflow = TextOverflow.Ellipsis
                                            )
                                            if(namePop) Popup(
                                                onDismissRequest = { namePop = false },
                                                properties = PopupProperties(focusable = true)
                                            ) {
                                                BasicTextField(item.name, {},Modifier.width(240.dp)
                                                    .border(0.5.dp,MaterialTheme.colorScheme.surfaceVariant)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                                    textStyle = LocalTextStyle.current,
                                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                                                )
                                            }
                                        }
                                        LazyRow(state = scrollState, userScrollEnabled = false) {
                                            items(table.limitKeyConfigs) {
                                                ResItemCell(Modifier.height(lineHeight)
                                                    .border(0.5.dp, MaterialTheme.colorScheme.surfaceVariant),hap,currKey,item.data[it])
                                            }
                                            item{
                                                Spacer(Modifier.width(120.dp))
                                            }
                                        }
                                    }
                                }}
                            }}
                        }
                    }}
                }
            }
        }
    }
}