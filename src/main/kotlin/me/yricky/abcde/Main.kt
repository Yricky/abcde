package me.yricky.abcde

import androidx.compose.animation.Crossfade
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import me.yricky.abcde.page.ClassListPage
import me.yricky.abcde.page.ClassViewPage
import me.yricky.abcde.page.CodeViewPage
import me.yricky.abcde.ui.Icons
import me.yricky.abcde.ui.isDarkTheme
import me.yricky.oh.abcd.AbcBuf
import java.io.File
import java.net.URI
import java.nio.channels.FileChannel
import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme(
        if(isDarkTheme()) darkColorScheme() else lightColorScheme()
    ) {
        Surface {
            var _appState:AppState? by remember {
                mutableStateOf(
//                    AppState(
//                        run {
//                            val file = File("/Users/yricky/Downloads/ets/modules.abc")
//                            val mmap = FileChannel.open(file.toPath()).map(FileChannel.MapMode.READ_ONLY,0,file.length())
//                            AbcBuf(mmap)
//                        }
//                    )
                    null
                )
            }
            Crossfade(_appState){ appState ->
                if(appState == null){
                    Box(Modifier.fillMaxSize()){
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Text("ABCDecoder", style = MaterialTheme.typography.displayLarge)
                            Row {
                                Text("OpenHarmony abc文件解析工具 by Yricky")
                            }
                            var isDragging by remember{ mutableStateOf(false) }
                            Box(
                                Modifier
                                    .padding(top = 60.dp)
                                    .size(320.dp,160.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(
                                        4.dp,
                                        color = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(16.dp)
                                    ).onExternalDrag(
                                        onDragStart = {
                                            isDragging = true
                                        },
                                        onDragExit = {
                                            isDragging = false
                                        },
                                        onDrag = {},
                                        onDrop = { state ->
                                            val dragData = state.dragData
                                            if (dragData is DragData.Image) {
                                            } else if (dragData is DragData.FilesList) {
                                                _appState = dragData.readFiles().mapNotNull {
                                                    File(URI(it)).takeIf {
                                                        it.isFile && it.extension.uppercase() == "ABC"
                                                    }
                                                }.firstOrNull()?.let {
                                                    AbcBuf(FileChannel.open(it.toPath())
                                                        .map(FileChannel.MapMode.READ_ONLY,0,it.length())
                                                    ).takeIf { it.header.isValid() }
                                                }?.let { AppState(it) }
                                            }
                                            isDragging = false
                                        }
                                    ).clickable {
                                        JFileChooser().apply {
                                            fileSelectionMode = JFileChooser.FILES_ONLY
                                            fileFilter = object : FileFilter() {
                                                override fun accept(pathname: File?): Boolean {
                                                    return pathname?.extension?.uppercase() == "ABC"
                                                }
                                                override fun getDescription(): String {
                                                    return "OpenHarmony字节码文件(*.abc)"
                                                }
                                            }
                                            showOpenDialog(null)
                                            _appState = selectedFile?.let {
                                                AbcBuf(FileChannel.open(it.toPath())
                                                    .map(FileChannel.MapMode.READ_ONLY,0,it.length())
                                                ).takeIf { it.header.isValid() }
                                            }?.let { AppState(it) }
                                        }

                                    }
                            ){
                                Text(
                                    "将文件拖动至此处或点击选择文件",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        LazyRow(Modifier.padding(horizontal = 4.dp).padding(top = 4.dp)) {
                            item {
                                Box(Modifier.size(28.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable { _appState = null },
                                ) {
                                    Image(Icons.homeFolder(),null, modifier = Modifier.align(Alignment.Center))
                                }
                            }
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(Icons.chevronRight(),null)
                                    Row(Modifier.height(28.dp).clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clickable { appState.clearPage() },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            appState.mainPage.tag,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                        )
                                    }
                                }

                            }
                            items(appState.pageStack){
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(Icons.chevronRight(),null)
                                    Row(Modifier.height(28.dp).clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clickable { appState.gotoPage(it) },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            it.tag,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                        )
                                    }
                                }
                            }
                        }
                        when(val currPage = appState.currPage){
                            is AppState.ClassList -> ClassListPage(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 4.dp),appState,appState.mainPage)
                            is AppState.ClassView -> ClassViewPage(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 4.dp),appState,currPage.classItem)
                            is AppState.CodeView ->   CodeViewPage(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 4.dp).padding(bottom = 4.dp),currPage.method,currPage.code)
                        }
                    }
                }
            }

        }

    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "ABCDecoder") {
        App()
    }
}
