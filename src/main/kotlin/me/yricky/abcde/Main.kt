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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.yricky.abcde.page.ClassListPage
import me.yricky.abcde.page.ClassViewPage
import me.yricky.abcde.page.CodeViewPage
import me.yricky.abcde.page.WelcomePage
import me.yricky.abcde.ui.AbcdeTheme
import me.yricky.abcde.ui.Icons
import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.isa.Asm
import java.awt.Dimension
import java.io.File
import java.nio.channels.FileChannel

@Composable
@Preview
fun App(initPath: String?) {
    AbcdeTheme {
        val appState: AppState = remember {
            AppState().apply {
                initPath?.let {
                    File(it).takeIf { it.isFile }
                }?.let {
                    AbcBuf(
                        it.path,
                        FileChannel.open(it.toPath())
                            .map(FileChannel.MapMode.READ_ONLY, 0, it.length())
                    ).takeIf { it.header.isValid() }
                }?.let { openAbc(it) }
            }
        }
        Column(Modifier.fillMaxSize()) {
            val scrollState = rememberLazyListState()
            val scope = rememberCoroutineScope()
            LazyRow(Modifier.pointerInput(PointerEventPass.Main){
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        if (event.type == PointerEventType.Scroll) {
                            event.changes.lastOrNull()?.scrollDelta?.let {
                                if(it.x == 0f && scrollState.layoutInfo.totalItemsCount > 1){
                                    if(it.y > 0){
                                        scope.launch {
                                            scrollState.animateScrollToItem(
                                                (scrollState.firstVisibleItemIndex + 1).coerceIn(0,scrollState.layoutInfo.totalItemsCount)
                                            )
                                        }
                                    } else if(it.y < 0){
                                        scope.launch {
                                            scrollState.animateScrollToItem(
                                                (scrollState.firstVisibleItemIndex - 1).coerceIn(0,scrollState.layoutInfo.totalItemsCount)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }.padding(horizontal = 4.dp).padding(top = 4.dp), state = scrollState) {
                item {
                    Box(
                        Modifier.size(28.dp).clip(CircleShape)
                            .let {
                                if (appState.currPage == null){
                                    it.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                } else it
                            }.background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { appState.currPage = null },
                    ) {
                        Image(Icons.homeFolder(), null, modifier = Modifier.align(Alignment.Center))
                    }
                }
                items(appState.pageStack) { p ->
                    var hover by remember {
                        mutableStateOf(false)
                    }
                    Row(Modifier.padding(start = 4.dp).height(28.dp).clip(RoundedCornerShape(14.dp))
                        .pointerInput(PointerEventPass.Main){
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Main)
                                    if (event.type == PointerEventType.Enter) {
                                       hover = true
                                    } else if (event.type == PointerEventType.Exit){
                                        hover = false
                                    }
                                }
                            }
                        }.let {
                                if (appState.currPage == p){
                                    it.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                                } else it
                            }.background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { appState.gotoPage(p) }.padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = if (!hover){
                                when (p) {
                                    is AppState.ClassList -> Icons.listFiles()
                                    is AppState.ClassView -> Icons.clazz()
                                    is AppState.CodeView -> Icons.method()
                                }
                            } else { Icons.close() },
                            null,
                            modifier = Modifier.aspectRatio(1f).clip(CircleShape).clickable {
                                appState.closePage(p)
                            }.padding(6.dp))
                        val title = remember(p) { p.tag.let { if(it.length > 35) "...${it.substring(it.length - 32, it.length)}" else it } }
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
            Crossfade(appState.currPage) { page ->
                when (page) {
                    null -> {
                        WelcomePage {
                            it?.let { abc -> appState.openAbc(abc) }
                        }
                    }

                    is AppState.ClassList -> ClassListPage(
                        Modifier.fillMaxWidth().weight(1f).padding(horizontal = 4.dp),
                        appState,
                        page
                    )

                    is AppState.ClassView -> ClassViewPage(
                        Modifier.fillMaxWidth().weight(1f).padding(horizontal = 4.dp),
                        appState,
                        page.classItem
                    )

                    is AppState.CodeView -> CodeViewPage(
                        Modifier.fillMaxWidth().weight(1f).padding(horizontal = 4.dp).padding(bottom = 4.dp),
                        appState,
                        page.method,
                        page.code
                    )
                }
            }
        }
    }
}
//val REGULAR_FONT = FontFamily(Font("fonts/HarmonyOS/HarmonyOS_Sans_SC_Regular.ttf"))
fun main(args: Array<String>) = application {
    println(args.toList())
    Window(onCloseRequest = ::exitApplication, title = "ABCDecoder") {
//        CompositionLocalProvider(
//            LocalTextStyle provides TextStyle(fontFamily = REGULAR_FONT)
//        ){
        val bgColor = MaterialTheme.colorScheme.surface
        LaunchedEffect(null){
            window.background = java.awt.Color(bgColor.value.toInt())
            window.minimumSize = Dimension(1280,800)
            launch(Dispatchers.IO){
                Asm.asmMap
            }
        }
        if(DesktopUtils.isLinux){
            CompositionLocalProvider(LocalDensity provides Density(1.5f,1f)){
                App(args.firstOrNull())
            }
        } else {
            App(args.firstOrNull())
        }
//        }
    }
}
