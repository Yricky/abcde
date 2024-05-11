package me.yricky.abcde.ui

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun LazyColumnWithScrollBar(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit
) {

    CompositionLocalProvider(
        LocalScrollbarStyle provides LocalScrollbarStyle.current.copy(
            unhoverColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
            hoverColor = MaterialTheme.colorScheme.tertiary
        )
    ){
        Box(modifier){
            LazyColumn(Modifier.fillMaxSize(),state, contentPadding, reverseLayout, verticalArrangement, horizontalAlignment, flingBehavior, userScrollEnabled, content)
            VerticalScrollbar(
                rememberScrollbarAdapter(state),
                Modifier.fillMaxHeight().align(Alignment.CenterEnd),
            )
        }
    }
}

fun Modifier.clearFocusWhenEnter(focus: FocusManager) = pointerInput("clearFocus"){
    awaitPointerEventScope {
        while (true){
            if(awaitPointerEvent().type == PointerEventType.Enter){
                focus.clearFocus()
            }
        }

    }
}

fun Modifier.requestFocusWhenEnter(focus: FocusRequester) = focusRequester(focus).pointerInput("requestFocus"){
    awaitPointerEventScope {
        while (true){
            when(awaitPointerEvent().type){
                PointerEventType.Enter -> {
                    focus.requestFocus()
                }
                PointerEventType.Exit -> {
                    focus.freeFocus()
                }
            }
        }

    }
}