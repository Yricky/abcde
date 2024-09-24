package me.yricky.abcde

import androidx.compose.animation.Crossfade
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.yricky.abcde.cli.CliEntry
import me.yricky.abcde.content.SettingsPanel
import me.yricky.abcde.desktop.DesktopUtils
import me.yricky.abcde.page.*
import me.yricky.abcde.ui.*
import me.yricky.abcde.util.SelectedFile
import me.yricky.oh.abcd.isa.Asm
import java.awt.Dimension
import java.io.File

@Composable
@Preview
fun App(appState: AppState) {
    Column(Modifier.fillMaxSize()) {
        val scrollState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        val session = appState.currHapSession
        Row(
            Modifier.padding(horizontal = 4.dp).padding(top = 4.dp).height(28.dp)
        ) {
            Row(Modifier.clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape)
                        .let {
                            if (session.currPage == null) {
                                it.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            } else it
                        }.clickable {
                            appState.currHapSession = appState.stubHapSession
                            appState.stubHapSession.goDefault()
                        },
                ) {
                    Image(Icons.homeFolder(), null, modifier = Modifier.align(Alignment.Center))
                }
                Row(Modifier.height(28.dp).clip(RoundedCornerShape(14.dp))
                    .let {
                        if (session.currPage == session.hapView && session.hapView != null) {
                            it.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                        } else it
                    }
                    .clickable { session.hapView?.let { session.gotoPage(it) } },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var popup by remember { mutableStateOf(false) }
                    Image(
                        painter = Icons.chevronDown(),
                        null,
                        modifier = Modifier.aspectRatio(1f).clip(CircleShape).clickable {
                            popup = true
                        }.padding(6.dp)
                    )
                    if(popup) Popup(alignment = Alignment.TopStart, onDismissRequest = { popup = false }) {
                        Surface(Modifier.size(360.dp), shape = RoundedCornerShape(0.dp,14.dp,14.dp,14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            LazyColumnWithScrollBar(Modifier.fillMaxSize()) {
                                item {
                                    Row(Modifier.fillMaxWidth().height(28.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { appState.currHapSession = appState.stubHapSession }
                                        .padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if(session == appState.stubHapSession) Image(Icons.checkMark(), null)
                                        Image(
                                            painter = Icons.stub(),
                                            null,
                                            modifier = Modifier.aspectRatio(1f).clip(CircleShape).padding(6.dp)
                                        )
                                        Text("未关联hap的页面",
                                            lineHeight = 14.sp,
                                            fontSize = 14.sp,
                                            )
                                    }
                                }
                                items(appState.hapSessions){
                                    Row(Modifier.fillMaxWidth().height(28.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { appState.currHapSession = it }
                                        .padding(start = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if(it == appState.currHapSession) Image(Icons.checkMark(), null)
                                        Image(
                                            painter = if (it.hapView != null) {
                                                it.hapView.iconDrawable() ?: Icons.archive()
                                            } else {
                                                Icons.stub()
                                            },
                                            null,
                                            modifier = Modifier.aspectRatio(1f).clip(CircleShape).padding(6.dp)
                                        )
                                        Text("${it.hapView?.shortName}",
                                            lineHeight = 14.sp,
                                            fontSize = 14.sp,
                                            modifier = Modifier.weight(1f),
                                            )
                                        Image(
                                            painter = Icons.close(),
                                            null,
                                            modifier = Modifier.aspectRatio(1f).clip(CircleShape)
                                                .clickable { appState.closeHap(it) }.padding(6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Image(
                        painter = if (session.hapView != null) {
                            key(session){ session.hapView.iconDrawable() } ?: Icons.archive()
                        } else {
                            Icons.stub()
                        },
                        null,
                        modifier = Modifier.aspectRatio(1f).clip(CircleShape).padding(6.dp)
                    )
                    session.hapView?.let {  hapView ->
                        val title = remember(session) { hapView.shortName }
                        Text(
                            title,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            lineHeight = 14.sp,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 8.dp),
                            maxLines = 1,
                        )
                    }
                }
            }
            LazyRow(Modifier.padding(start = 4.dp).pointerInput(PointerEventPass.Main) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        if (event.type == PointerEventType.Scroll) {
                            event.changes.lastOrNull()?.scrollDelta?.let {
                                if (it.x == 0f && scrollState.layoutInfo.totalItemsCount > 1) {
                                    if (it.y > 0) {
                                        scope.launch {
                                            scrollState.animateScrollToItem(
                                                (scrollState.firstVisibleItemIndex + 1).coerceIn(
                                                    0,
                                                    scrollState.layoutInfo.totalItemsCount
                                                )
                                            )
                                        }
                                    } else if (it.y < 0) {
                                        scope.launch {
                                            scrollState.animateScrollToItem(
                                                (scrollState.firstVisibleItemIndex - 1).coerceIn(
                                                    0,
                                                    scrollState.layoutInfo.totalItemsCount
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }, state = scrollState) {
                items(session.pageStack) { p ->
                    var hover by remember {
                        mutableStateOf(false)
                    }
                    Row(Modifier.padding(end = 4.dp).height(28.dp).clip(RoundedCornerShape(14.dp))
                        .hover { hover = it }.let {
                            if (session.currPage == p) {
                                it.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                            } else it
                        }.background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { session.gotoPage(p) }.padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = if (!hover) {
                                when (p) {
                                    is AbcView -> Icons.listFiles()
                                    is ClassView -> p.classItem.icon()
                                    is CodeView -> p.code.method.icon()
                                    is ResIndexView -> Icons.indexCluster()
                                }
                            } else {
                                Icons.close()
                            },
                            null,
                            modifier = Modifier.aspectRatio(1f).clip(CircleShape).clickable {
                                session.closePage(p)
                            }.padding(6.dp)
                        )
                        val title = remember(p) { p.shortName }
                        Text(
                            title,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            lineHeight = 14.sp,
                            fontSize = 14.sp,
                            modifier = Modifier,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        Crossfade(session.currPage) { page ->
            when (page) {
                null -> {
                    WelcomePage(appState) { appState.open(it) }
                }
                else -> {
                    page.Page(Modifier.fillMaxWidth().weight(1f), session, appState)
                }
            }
        }
    }
}

fun main(args: Array<String>) = if(args.firstOrNull() == "--cli") {
    CliEntry(args.toMutableList().also { it.removeAt(0) }).run()
} else run {
    println(args.toList())
    val filePath = args.lastOrNull { !it.startsWith("-") }
    GlobalScope.launch {
        withContext(Dispatchers.IO){
            Asm.innerAsmMap
        }
    }
    application {
        val appState: AppState = remember {
            AppState().apply {
                filePath?.let {
                    File(it).takeIf { it.isFile }
                }?.let {
                    SelectedFile.fromOrNull(it)?.let { open(it) }
                }
            }
        }
        ABCDEWindow(onCloseRequest = ::exitApplication, title = "ABCDecoder") {
            LaunchedEffect(null){
                DesktopUtils.AppStatus.renderApi = window.renderApi
                window.minimumSize = Dimension(1280,800)
            }

            AbcdeFrame(appState) {
                App(appState)
            }
        }
        SettingsPanel(appState.showSettings){ appState.showSettings = false }
    }
}