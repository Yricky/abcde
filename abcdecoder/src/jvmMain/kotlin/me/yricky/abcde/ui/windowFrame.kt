package me.yricky.abcde.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import me.yricky.abcde.desktop.DesktopUtils


class ABCDEWindowScope(
    private val frameWindowScope: FrameWindowScope,
    private val _cfg:State<DesktopUtils.AppConfig>
):FrameWindowScope by frameWindowScope{
    val cfg by _cfg
}
@Composable
fun ABCDEWindow(
    onCloseRequest: () -> Unit,
    state: WindowState = rememberWindowState(),
    visible: Boolean = true,
    title: String = "ABCDecoder",
    icon: Painter? = null,
    undecorated: Boolean = false,
    transparent: Boolean = false,
    resizable: Boolean = true,
    enabled: Boolean = true,
    focusable: Boolean = true,
    alwaysOnTop: Boolean = false,
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    content: @Composable ABCDEWindowScope.() -> Unit
){
    Window(
        onCloseRequest,
        state,
        visible,
        title,
        icon,
        undecorated,
        transparent,
        resizable,
        enabled,
        focusable,
        alwaysOnTop,
        onPreviewKeyEvent,
        onKeyEvent
    ){
        val cfg = DesktopUtils.AppConfig.flow.collectAsState()
        val windowScope = remember(this) { ABCDEWindowScope(this,cfg) }
        val density by remember { derivedStateOf { cfg.value.density } }
        Crossfade(isDarkTheme()) { b ->
            MaterialTheme(
                colorScheme = if (b) darkColorScheme() else lightColorScheme(),
            ) {
                val bgColor = MaterialTheme.colorScheme.background
                LaunchedEffect(null){
                    window.background = java.awt.Color(bgColor.value.toInt())
                }
                CompositionLocalProvider(
                    LocalScrollbarStyle provides LocalScrollbarStyle.current.copy(
                        unhoverColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        hoverColor = MaterialTheme.colorScheme.tertiary
                    ),
                    LocalDensity provides Density(density,1f)
                ) {
                    Box(Modifier.background(MaterialTheme.colorScheme.background)) {
                        windowScope.content()
                    }
                }
            }
        }
    }
}