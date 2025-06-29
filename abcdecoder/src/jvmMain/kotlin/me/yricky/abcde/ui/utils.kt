package me.yricky.abcde.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import me.yricky.abcde.AppState
import me.yricky.abcde.util.SelectedFile
import me.yricky.oh.abcd.cfm.*
import me.yricky.oh.abcd.literal.LiteralArray
import java.io.File
import java.net.URI

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun AbcdeFrame(appState: AppState, content:@Composable ()->Unit) {
    var draggingData by remember { mutableStateOf<List<SelectedFile>?>(null) }
    Surface(Modifier.dragAndDropTarget(
        shouldStartDragAndDrop = {
            it.dragData() is DragData.FilesList
        },
        object :DragAndDropTarget{
            override fun onDrop(event: DragAndDropEvent): Boolean {
                println("onDrop")

                val dragData = event.dragData()
                if (dragData is DragData.FilesList) {
                    draggingData = runCatching {
                        dragData.readFiles().mapNotNull { s ->
                            SelectedFile.fromOrNull(File(URI(s)))
                                ?.takeIf { it.valid() }
                        }
                    }.onFailure {
                        it.printStackTrace()
                    }.getOrNull()
                }
                draggingData?.forEach{
                    appState.open(it)
                }
                draggingData = null
                return true
            }

            override fun onEntered(event: DragAndDropEvent) {
                println("onEntered")
            }

            override fun onExited(event: DragAndDropEvent) {
                println("onExited")
                draggingData = null
            }

            override fun onEnded(event: DragAndDropEvent) {
                println("onEnded")
                draggingData = null
            }

            override fun onStarted(event: DragAndDropEvent) {
                println("onStarted")
                val dragData = event.dragData()
                if (dragData is DragData.FilesList) {
                    draggingData = runCatching {
                        dragData.readFiles().mapNotNull { s ->
                            SelectedFile.fromOrNull(File(URI(s)))
                                ?.takeIf { it.valid() }
                        }
                    }.onFailure {
                        it.printStackTrace()
                    }.getOrNull()
                }
            }
        }
    )) {
        Box(
            Modifier.fillMaxSize()){
            val blur by animateDpAsState(if(draggingData != null) 16.dp else 0.dp)
            Box(
                Modifier.let { if(blur != 0.dp) it.blur(blur) else it }
            ){
                content()
            }
            draggingData?.let {
                Box(Modifier.fillMaxSize()
                    .alpha(0.5f)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                ){
                    if(it.isEmpty()){
                        Text(
                            "ABCDecoder无法打开拖入的文件",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.headlineLarge
                        )
                    } else {
                        Text(
                            "松开后，ABCDecoder将打开拖入的${it.size}个文件",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun LazyColumnWithScrollBar(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit
) {

    CompositionLocalProvider(
        LocalScrollbarStyle provides LocalScrollbarStyle.current.copy(
            unhoverColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
            hoverColor = MaterialTheme.colorScheme.tertiary
        )
    ){
        Box(modifier){
            LazyColumn(
                Modifier.fillMaxSize(),
                state,
                contentPadding,
                reverseLayout,
                verticalArrangement,
                horizontalAlignment,
                flingBehavior,
                userScrollEnabled,
                content = content
            )
            VerticalScrollbar(
                rememberScrollbarAdapter(state),
                Modifier.fillMaxHeight().align(Alignment.CenterEnd),
            )
        }
    }
}

fun Modifier.hover(onHover:(Boolean) -> Unit) = pointerInput(PointerEventPass.Main) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Main)
            if (event.type == PointerEventType.Enter) {
                onHover(true)
            } else if (event.type == PointerEventType.Exit) {
                onHover(false)
            }
        }
    }
}

fun Modifier.clearFocusWhenEnter(focus: FocusManager) = pointerInput("clearFocus"){
    awaitPointerEventScope {
        while (true){
            if(awaitPointerEvent().type == PointerEventType.Enter){
                focus.clearFocus()
            }
        }

    }
}

fun Modifier.requestFocusWhenEnter(focus: FocusRequester) = focusRequester(focus).pointerInput("requestFocus"){
    awaitPointerEventScope {
        while (true){
            when(awaitPointerEvent().type){
                PointerEventType.Enter -> {
                    focus.requestFocus()
                }
                PointerEventType.Exit -> {
                    focus.freeFocus()
                }
            }
        }
    }
}

@Composable
fun VerticalTabAndContent(
    modifier: Modifier,
    tabState:MutableState<Int>,
    tabAndContent:List<Pair<@Composable (Boolean)->Unit,@Composable ()->Unit>>
){
    var index by tabState
    Row(modifier.background(MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxHeight().width(36.dp).padding(start = 4.dp, end = 4.dp, top = 4.dp)) {
            tabAndContent.forEachIndexed { i,it ->
                val selected = index == i
                Box(Modifier.aspectRatio(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if(selected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface)
                    .clickable { index = i }
                    .padding(4.dp)) {
                    it.first(selected)
                }
                Spacer(Modifier.height(2.dp))
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight()) {
            tabAndContent[index].second()
        }
    }
}

fun composeSelectContent(content:@Composable (Boolean)->Unit) = content
fun composeContent(content:@Composable ()->Unit) = content

fun Long.toByteSizeFormat():String = kotlin.run {
    if(this > 1048576){
        "${String.format("%.2f",this/1048576.0)}MiB"
    } else if(this > 1024){
        "${String.format("%.2f",this/1024.0)}KiB"
    } else {
        "${this}B"
    }
}

fun String.short(maxLen:Int = 35) = if (length > maxLen) "...${
    substring(
        length - maxLen + 3,
        length
    )
}" else this

fun AbcField.defineStr():String = run {
    val sb = StringBuilder()
    if(accessFlags.isPublic){
        sb.append("public ")
    }
    if(accessFlags.isPrivate){
        sb.append("private ")
    }
    if(accessFlags.isProtected){
        sb.append("protected ")
    }
    if(accessFlags.isStatic){
        sb.append("static ")
    }
    if(accessFlags.isFinal){
        sb.append("final ")
    }
    if(accessFlags.isVolatile){
        sb.append("volatile ")
    }

    sb.append("${type.primitiveType} $name")
    if(isModuleRecordIdx()){
        val moduleRecordOffset = getIntValue()
        sb.append("= 0x${moduleRecordOffset?.toString(16)}")
    } else if(isScopeNames()){
        getIntValue()?.let {
            abc.literalArray(it)
        }?.let {
            sb.append("= $it")
        }
    } else {
        val moduleRecordOffset = getIntValue()
        sb.append("= 0x${moduleRecordOffset?.toString(16)}")
    }
    sb.toString()
}

fun MethodItem.defineStr(showClass:Boolean = false):String = run {
    val sb = StringBuilder()
//    if(indexData.isPublic){
//        sb.append("public ")
//    }
//    if(indexData.isPrivate){
//        sb.append("private ")
//    }
//    if(indexData.isProtected){
//        sb.append("protected ")
//    }
//    if(indexData.isStatic){
//        sb.append("static ")
//    }
//    if(indexData.isAbstract){
//        sb.append("abstract ")
//    }
//    if(indexData.isFinal){
//        sb.append("final ")
//    }
//    if(accessFlags.isNative){
//        sb.append("native ")
//    }
//    if(indexData.isSynchronized){
//        sb.append("synchronized ")
//    }
//    sb.append("${proto?.shortyReturn ?: ""} ")
    if(showClass && clazz != null){
        sb.append("${clazz?.name}.")
    }
    sb.append(name)
    sb.append(argsStr())
    sb.toString()
}