package me.yricky.abcde.content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.yricky.abcde.desktop.DesktopUtils
import me.yricky.abcde.ui.ABCDEWindow
import me.yricky.abcde.ui.Icons
import me.yricky.abcde.ui.isDarkTheme

@Composable
fun SettingsPanel(show:Boolean,onDismiss:()->Unit){
    ABCDEWindow(
        onDismiss,
        visible = show,
        icon = Icons.editorConfig(),
        title = "设置"
    ){
        Column(Modifier.fillMaxSize().padding(8.dp)) {
            OutlinedCard() {
                var newDensity by remember { mutableStateOf(cfg.density) }
                val scope = rememberCoroutineScope()
                Text("DPI缩放:${String.format("%.02f",newDensity)}", modifier = Modifier.padding(horizontal = 8.dp))
                Slider(
                    value = newDensity,
                    onValueChange = { newDensity = it },
                    onValueChangeFinished = {
                        scope.launch {
                            DesktopUtils.AppConfig.edit {
                                it.copy(density = newDensity)
                            }
                        }
                    },
                    valueRange = 0.5f .. 3f,
                    steps = 49
                )
                Text("颜色主题")
                Icon(if(isDarkTheme()) Icons.darkTheme() else Icons.lightTheme(), null, Modifier.size(28.dp).clip(
                    CircleShape
                ).clickable {
                    scope.launch {
                        DesktopUtils.AppConfig.edit{
                            it.copy(darkTheme = it.darkTheme?.not() ?: false)
                        }
                    }
                }.padding(4.dp))
            }
        }
    }
}