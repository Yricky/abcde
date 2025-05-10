package me.yricky.abcde.page

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.yricky.abcde.AppState
import me.yricky.abcde.content.VersionPanel
import me.yricky.abcde.desktop.DesktopUtils
import me.yricky.abcde.desktop.abcFileChooser
import me.yricky.abcde.desktop.config.HistoryConfig
import me.yricky.abcde.ui.Icons
import me.yricky.abcde.ui.codeStyle
import me.yricky.abcde.ui.hover
import me.yricky.abcde.ui.short
import me.yricky.abcde.util.SelectedFile
import java.io.File
import java.nio.file.Path
import javax.swing.JFileChooser
import kotlin.io.path.exists

@Composable
fun WelcomePage(
    appState: AppState,
    openAction: (SelectedFile) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text("ABCDecoder", style = MaterialTheme.typography.displayLarge)
            Row {
                Text("OpenHarmony逆向工具 by Yricky")
            }
            Box(
                Modifier
                    .padding(top = 60.dp)
                    .size(480.dp, 240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        4.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                val history by HistoryConfig.flow.collectAsState()
                if(history.openedFile.isNotEmpty()){
                    Column(Modifier.align(Alignment.Center)){
                        Box(Modifier.fillMaxWidth().height(60.dp).clickable {
                            JFileChooser().apply {
                                fileSelectionMode = JFileChooser.FILES_ONLY
                                fileFilter = abcFileChooser
                                showOpenDialog(null)
                                if(selectedFile?.isFile == true){
                                    selectedFile?.let { SelectedFile.fromOrNull(it)?.let(openAction) }
                                }
                            }
                        }){
                            Text(
                                "将文件拖入窗口或点击此处选择文件",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        val scope = rememberCoroutineScope()
                        LazyColumn(Modifier.fillMaxWidth().weight(1f)){
                            items(history.openedFile.asReversed()){
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable{
                                    SelectedFile.fromOrNull(File(it.path))?.let(openAction)
                                }.fillMaxWidth().padding(4.dp)){

                                    Text(it.path.short(48), Modifier.weight(1f),
                                        color = if(Path.of(it.path).exists()){
                                            Color.Unspecified
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        },
                                        style = codeStyle,
                                        maxLines = 1
                                    )
                                    Icon(Icons.close(),null, modifier = Modifier.padding(end=4.dp).size(16.dp).clip(CircleShape).clickable{
                                        scope.launch(Dispatchers.IO) {
                                            HistoryConfig.edit { config -> config.copy(openedFile = config.openedFile.filter { file -> it != file }) }
                                        }
                                    })
                                }

                            }
                        }
                    }
                }else {
                    Box(Modifier.fillMaxSize().clickable {
                        JFileChooser().apply {
                            fileSelectionMode = JFileChooser.FILES_ONLY
                            fileFilter = abcFileChooser
                            showOpenDialog(null)
                            if (selectedFile?.isFile == true) {
                                selectedFile?.let { SelectedFile.fromOrNull(it)?.let(openAction) }
                            }
                        }
                    }){
                        Text(
                            "将文件拖入窗口或点击此处选择文件",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                }
            }
        }
        Row(
            modifier = Modifier.align(Alignment.BottomStart).height(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.editorConfig(), null, Modifier.size(28.dp).clip(CircleShape).clickable {
                appState.showSettings = !appState.showSettings
            }.padding(4.dp))
            Spacer(Modifier.weight(1f))
            var showPopup by remember {
                mutableStateOf(false)
            }
            Text("版本:${DesktopUtils.properties["version"]}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.hover { showPopup = showPopup or it })
            if(showPopup){
                Popup(
                    alignment = Alignment.TopEnd,
                    offset = IntOffset(0, (LocalDensity.current.density * -240).toInt()),
                    onDismissRequest = { showPopup = false }
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape =  RoundedCornerShape(12.dp),
                        modifier = Modifier.size(240.dp).padding(4.dp)
                    ) {
                        VersionPanel(Modifier.padding(12.dp).verticalScroll(rememberScrollState()))
                    }
                }
            }
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