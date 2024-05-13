package me.yricky.abcde.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun VerticalTabAndContent(
    modifier: Modifier,
    tabAndContent:List<Pair<@Composable (Boolean)->Unit,@Composable ()->Unit>>
){
    var index by remember { mutableIntStateOf(0) }
    Row(modifier.background(MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxHeight().width(32.dp).padding(end = 4.dp, top = 4.dp)) {
            tabAndContent.forEachIndexed { i,it ->
                val selected = index == i
                Box(Modifier.aspectRatio(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if(selected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface)
                    .clickable { index = i }
                    .padding(4.dp)) {
                    it.first(selected)
                }
                Spacer(Modifier.height(2.dp))
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight()) {
            tabAndContent[index].second()
        }
    }
}

fun composeSelectContent(content:@Composable (Boolean)->Unit) = content
fun composeContent(content:@Composable ()->Unit) = content