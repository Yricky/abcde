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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.yricky.abcde.desktop.DesktopUtils
import me.yricky.abcde.desktop.abcFileChooser
import me.yricky.abcde.desktop.hapFileChooser
import me.yricky.abcde.desktop.resIndexFileChooser
import me.yricky.abcde.ui.Icons
import me.yricky.abcde.ui.isDarkTheme
import me.yricky.abcde.util.SelectedFile
import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.AbcHeader
import me.yricky.oh.common.wrapAsLEByteBuf
import java.io.File
import java.net.URI
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WelcomePage(
    setAppState: (SelectedFile) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text("ABCDecoder", style = MaterialTheme.typography.displayLarge)
            Row {
                Text("OpenHarmony abc文件解析工具 by Yricky")
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
                            if (dragData is DragData.FilesList) {
                                dragData.readFiles().mapNotNull {
                                    SelectedFile.fromOrNull(File(URI(it)))?.let(setAppState)
                                }
                            }
                            isDragging = false
                        }
                    ).clickable {
                        JFileChooser().apply {
                            fileSelectionMode = JFileChooser.FILES_ONLY
                            fileFilter = abcFileChooser
                            addChoosableFileFilter(resIndexFileChooser)
                            addChoosableFileFilter(hapFileChooser)
                            showOpenDialog(null)
                            if(selectedFile?.isFile == true){
                                selectedFile?.let { SelectedFile.fromOrNull(it)?.let(setAppState) }
                            }
                        }

                    }
            ) {
                Text(
                    "将文件拖入或点击此处选择文件",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        Row(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(if(isDarkTheme()) Icons.darkTheme() else Icons.lightTheme(), null, Modifier.size(28.dp).clip(CircleShape).clickable {
                isDarkTheme.value = !isDarkTheme.value
            }.padding(4.dp))
            Spacer(Modifier.weight(1f))
            Text("版本:${DesktopUtils.properties["version"]}", style = MaterialTheme.typography.bodySmall)
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