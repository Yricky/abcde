package me.yricky.abcde.page

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import me.yricky.abcde.AppState
import me.yricky.abcde.DesktopUtils
import me.yricky.oh.abcd.AbcBuf
import java.io.File
import java.net.URI
import java.nio.channels.FileChannel
import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WelcomePage(
    setAppState: (AppState?) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text("ABCDecoder", style = MaterialTheme.typography.displayLarge)
            Row {
                Text("OpenHarmony abc文件解析工具 by Yricky")
                Text(
                    "联系作者",
                    color = Color.Cyan,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        DesktopUtils.chatToMe()
                    })
            }
            var isDragging by remember { mutableStateOf(false) }
            Box(
                Modifier
                    .padding(top = 60.dp)
                    .size(320.dp, 160.dp)
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
                                setAppState(
                                    dragData.readFiles().mapNotNull {
                                        File(URI(it)).takeIf {
                                            it.isFile && it.extension.uppercase() == "ABC"
                                        }
                                    }.firstOrNull()?.let {
                                        AbcBuf(
                                            FileChannel.open(it.toPath())
                                                .map(FileChannel.MapMode.READ_ONLY, 0, it.length())
                                        ).takeIf { it.header.isValid() }
                                    }?.let { AppState(it) }
                                )
                            }
                            isDragging = false
                        }
                    ).clickable {
                        JFileChooser().apply {
                            fileSelectionMode = JFileChooser.FILES_ONLY
                            fileFilter = object : FileFilter() {
                                override fun accept(pathname: File?): Boolean {
                                    return pathname?.extension?.uppercase() == "ABC" || (pathname?.isDirectory == true)
                                }

                                override fun getDescription(): String {
                                    return "OpenHarmony字节码文件(*.abc)"
                                }
                            }
                            showOpenDialog(null)
                            if(selectedFile?.isFile == true){
                                setAppState(
                                    selectedFile?.let {
                                        AbcBuf(
                                            FileChannel.open(it.toPath())
                                                .map(FileChannel.MapMode.READ_ONLY, 0, it.length())
                                        ).takeIf { it.header.isValid() }
                                    }?.let { AppState(it) }
                                )
                            }
                        }

                    }
            ) {
                Text(
                    "将文件拖动至此处或点击选择文件",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        Row(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Icon(
                painterResource("ic/gitee.svg"),
                null,
                Modifier.size(28.dp).padding(4.dp).clip(CircleShape).clickable {
                    DesktopUtils.openUrl("https://gitee.com/sjtuYricky/abcde")
                }
            )
            Icon(
                painterResource("ic/github.svg"),
                null,
                Modifier.size(28.dp).padding(4.dp).clip(CircleShape).clickable {
                    DesktopUtils.openUrl("https://github.com/Yricky/abcde")
                }
            )
        }
    }
}