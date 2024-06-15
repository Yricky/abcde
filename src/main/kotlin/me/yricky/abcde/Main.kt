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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import me.yricky.abcde.page.ClassListPage
import me.yricky.abcde.page.ClassViewPage
import me.yricky.abcde.page.CodeViewPage
import me.yricky.abcde.page.WelcomePage
import me.yricky.abcde.ui.Icons
import me.yricky.abcde.ui.isDarkTheme
import me.yricky.oh.abcd.AbcBuf
import java.io.File
import java.nio.channels.FileChannel

@Composable
@Preview
fun App(initPath: String?) {
    MaterialTheme(
        colorScheme = if (isDarkTheme()) darkColorScheme() else lightColorScheme(),
    ) {
        Surface {
            var _appState: AppState? by remember {
                mutableStateOf(
                    initPath?.let {
                        File(it).takeIf { it.isFile }
                    }?.let {
                        AbcBuf(
                            FileChannel.open(it.toPath())
                                .map(FileChannel.MapMode.READ_ONLY, 0, it.length())
                        ).takeIf { it.header.isValid() }
                    }?.let { AppState(it) }
                )
            }
            Crossfade(_appState) { appState ->
                if (appState == null) {
                    WelcomePage {
                        _appState = it
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        LazyRow(Modifier.padding(horizontal = 4.dp).padding(top = 4.dp)) {
                            item {
                                Box(
                                    Modifier.size(28.dp).clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clickable { _appState = null },
                                ) {
                                    Image(Icons.homeFolder(), null, modifier = Modifier.align(Alignment.Center))
                                }
                            }
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(Icons.chevronRight(), null)
                                    Row(
                                        Modifier.height(28.dp).clip(RoundedCornerShape(14.dp))
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
                            items(appState.pageStack) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(Icons.chevronRight(), null)
                                    Row(
                                        Modifier.height(28.dp).clip(RoundedCornerShape(14.dp))
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
                        when (val currPage = appState.currPage) {
                            is AppState.ClassList -> ClassListPage(
                                Modifier.fillMaxWidth().weight(1f).padding(horizontal = 4.dp),
                                appState,
                                appState.mainPage
                            )

                            is AppState.ClassView -> ClassViewPage(
                                Modifier.fillMaxWidth().weight(1f).padding(horizontal = 4.dp),
                                appState,
                                currPage.classItem
                            )

                            is AppState.CodeView -> CodeViewPage(
                                Modifier.fillMaxWidth().weight(1f).padding(horizontal = 4.dp).padding(bottom = 4.dp),
                                currPage.method,
                                currPage.code
                            )
                        }
                    }
                }
            }

        }

    }
}
//val REGULAR_FONT = FontFamily(Font("fonts/HarmonyOS/HarmonyOS_Sans_SC_Regular.ttf"))
fun main(args: Array<String>) = application {
    println(args.toList())
    val isLinux = System.getProperty("os.name") == "Linux"
    Window(onCloseRequest = ::exitApplication, title = "ABCDecoder") {
//        CompositionLocalProvider(
//            LocalTextStyle provides TextStyle(fontFamily = REGULAR_FONT)
//        ){
        if(isLinux){
            CompositionLocalProvider(LocalDensity provides Density(1.5f,1f)){
                App(args.firstOrNull())
            }
        } else {
            App(args.firstOrNull())
        }
//        }
    }
}
