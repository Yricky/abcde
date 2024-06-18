//package me.yricky.abcde.ui
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.unit.dp
//import kotlinx.coroutines.flow.MutableStateFlow
//import java.nio.ByteBuffer
//
//@Composable
//fun HexView(
//    modifier: Modifier = Modifier,
//    state: HexViewState,
//){
//    val startIndexPadding = remember(state.startIndex) { state.startIndex / 16 }
//    val endIndexPadding = remember(state.endIndexExclude) { (state.endIndexExclude+15) / 16 }
//    val listState = rememberLazyListState()
//    LazyColumn(modifier,listState) {
//        items(endIndexPadding - startIndexPadding, contentType = { 0 }){ metaIdx ->
//            val idx = metaIdx * 16
//            Row {
//                Text(
//                    text = String.format("%08X",idx),
//                    fontFamily = FontFamily.Monospace
//                )
//                repeat(16){ _idx ->
//                    val thisIdx = idx + _idx
//                    Text(
//                        text = if(thisIdx < state.endIndexExclude) String.format("%02X",state.buf.get(thisIdx)) else "  ",
//                        fontFamily = FontFamily.Monospace,
//                        maxLines = 1,
//                        modifier = Modifier.background(
//                            state.bgColorOf(thisIdx) ?: MaterialTheme.colorScheme.background
//                        ).padding(2.dp),
//                        color = state.colors(thisIdx) ?: MaterialTheme.colorScheme.onBackground
//                    )
//                    if(thisIdx%16 == 7){
//                        Spacer(Modifier.width(1.dp))
//                    }
//                }
//            }
//        }
//    }
//    val initIndex by state.initIndex.collectAsState()
//    LaunchedEffect(initIndex){
//        initIndex?.let {
//            listState.scrollToItem((it/16 - startIndexPadding).coerceIn(0,listState.layoutInfo.totalItemsCount - 1))
//            state.initIndex.value = null
//        }
//    }
////    LaunchedEffect(listState.firstVisibleItemIndex){
////        if(state.initIndex != listState.firstVisibleItemIndex) {
////            state.initIndex = listState.firstVisibleItemIndex
////        }
////    }
//}
//
//class HexViewState(
//    val buf: ByteBuffer,
//    val startIndex:Int = 0,
//    val endIndexExclude:Int = buf.limit(),
//    val onClick: (Int) -> ((Int) -> Unit)? = { null }
//){
//    val colorList = mutableStateListOf<Pair<IntRange,Color>>()
//    val backgroundColorList = mutableStateListOf<Pair<IntRange,Color>>()
//
//    fun colors(index:Int):Color? = colorList.firstOrNull { it.first.contains(index) }?.second
//    fun bgColorOf(index: Int):Color? = backgroundColorList.firstOrNull { it.first.contains(index) }?.second
//
//    val initIndex:MutableStateFlow<Int?> =  MutableStateFlow(null)
//}
