package me.yricky.abcde.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import me.yricky.oh.common.TreeStruct

@Composable
fun <T> TreeItemList(
    modifier: Modifier,
    list: List<Pair<Int, TreeStruct.Node<T>>>,
    onClick: (TreeStruct.Node<T>) -> Unit = {},
    content: @Composable RowScope.(TreeStruct.Node<T>) -> Unit = {}
) {
    val state = rememberLazyListState()
    Box(modifier){
        LazyColumnWithScrollBar(
            Modifier.fillMaxSize(),
            state
        ) {
            items(list) { item ->
                val density = LocalDensity.current
                Row(
                    Modifier.fillMaxWidth()
                        .clickable { onClick(item.second) }.drawBehind {
                            repeat(item.first){
                                drawRect(
                                    Color.hsv(((it * 40)%360).toFloat() ,1f,0.5f),
                                    topLeft = Offset(density.density * (it * 12 + 5),0f),
                                    size = Size(density.density * 2,size.height)
                                )
                            }
                        }.padding(start = (12*item.first).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    content(item.second)
                }
            }
        }
    }
}