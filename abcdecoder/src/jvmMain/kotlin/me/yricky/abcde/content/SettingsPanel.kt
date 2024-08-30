package me.yricky.abcde.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.yricky.abcde.desktop.DesktopUtils
import me.yricky.abcde.ui.*

@Composable
fun SettingsPanel(show:Boolean,onDismiss:()->Unit){
    ABCDEWindow(
        onDismiss,
        visible = show,
        icon = Icons.editorConfig(),
        title = "设置"
    ){
        val cfg = LocalAppConfig.current
        val scope = rememberCoroutineScope()
        Column(Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
            ConfigGroup("外观",Modifier.padding(top=8.dp)) {
                var newDensity by remember { mutableStateOf(cfg.density) }
                Text(
                    "DPI缩放:${String.format("%.02f",newDensity)}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                )
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

                Row(
                    Modifier.height(40.dp).clickable {
                        scope.launch {
                            DesktopUtils.AppConfig.edit{
                                it.copy(darkTheme = it.darkTheme?.not() ?: false)
                            }
                        }
                    }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "颜色主题",Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Icon(if(isDarkTheme()) Icons.darkTheme() else Icons.lightTheme(), null, Modifier.size(20.dp).clip(
                        CircleShape
                    ))
                }
            }
            ConfigGroup("进阶设置",Modifier.padding(top=8.dp)){
                Row(
                    Modifier.height(40.dp).clickable {
                        scope.launch {
                            DesktopUtils.AppConfig.edit{
                                it.copy(futureFeature = it.futureFeature.not())
                            }
                        }
                    }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "启用开发中功能",Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Checkbox(cfg.futureFeature,{})
                }
            }
        }
    }
}

@Composable
fun ConfigGroup(
    title:String,
    modifier: Modifier = Modifier,
    content:@Composable ColumnScope.()->Unit
){
    Column(modifier) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 8.dp))
        OutlinedCard {
            content()
        }
    }
}