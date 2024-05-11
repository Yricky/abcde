package me.yricky.abcde.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import java.nio.ByteBuffer

@Composable
fun HexView(
    modifier: Modifier = Modifier,
    state: HexViewState,
){
    val startIndexPadding = remember(state.startIndex) { state.startIndex / 16 }
    val endIndexPadding = remember(state.endIndexExclude) { (state.endIndexExclude+15) / 16 }
    val listState = rememberLazyListState()
    LazyColumn(modifier,listState) {
        items(endIndexPadding - startIndexPadding, contentType = { 0 }){ metaIdx ->
            val idx = metaIdx * 16
            Row {
                Text(
                    text = String.format("%08X",idx),
                    fontFamily = FontFamily.Monospace
                )
                repeat(16){ _idx ->
                    val thisIdx = idx + _idx
                    Text(
                        text = if(thisIdx < state.endIndexExclude) String.format("%02X",state.buf.get(thisIdx)) else "  ",
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        modifier = Modifier.background(
                            state.backGroundColors(thisIdx) ?: MaterialTheme.colorScheme.background
                        ).padding(2.dp),
                        color = state.colors(thisIdx) ?: MaterialTheme.colorScheme.onBackground
                    )
                    if(thisIdx%16 == 7){
                        Spacer(Modifier.width(1.dp))
                    }
                }
            }
        }
    }
    LaunchedEffect(state.initIndex){
        listState.scrollToItem((state.initIndex/16 - startIndexPadding).coerceIn(0,listState.layoutInfo.totalItemsCount - 1))
    }
//    LaunchedEffect(listState.firstVisibleItemIndex){
//        if(state.initIndex != listState.firstVisibleItemIndex) {
//            state.initIndex = listState.firstVisibleItemIndex
//        }
//    }
}

class HexViewState(
    val buf: ByteBuffer,
    val startIndex:Int = 0,
    val endIndexExclude:Int = buf.limit(),
    val colors: @Composable (Int) -> Color? = { null },
    val backGroundColors: @Composable (Int) -> Color? = { null },
    val onClick: (Int) -> ((Int) -> Unit)? = { null }
){
    var initIndex:Int by mutableStateOf(startIndex)
}
