package me.yricky.abcde.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
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
    expand: (TreeStruct.TreeNode<T>) -> Boolean,
    onClick: (TreeStruct.Node<T>) -> Unit = {},
    applyContent: LazyListScope.(LazyListScope.() -> Unit) -> Unit = { it() },
    withTreeHeader: Boolean = true,
    content: @Composable RowScope.(TreeStruct.Node<T>) -> Unit
) {
    val state = rememberLazyListState()
    Box(modifier){
        val density = LocalDensity.current
        LazyColumnWithScrollBar(
            Modifier.fillMaxSize(),
            state
        ) {
            applyContent{
                items(list, key = { "${it.first}/${it.second.path}${it.second.javaClass}" }, contentType = { 1 }) { item ->
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable { onClick(item.second) }.drawBehind {
                                repeat(item.first){
                                    drawRect(
                                        Color.hsv(((it * 40)%360).toFloat() ,1f,0.5f),
                                        topLeft = Offset(density.density * (it * 12 + 7),0f),
                                        size = Size(density.density * 2,size.height)
                                    )
                                }
                            }.padding(start = (12*item.first).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (val node = item.second) {
                            is TreeStruct.LeafNode -> {
                                Spacer(Modifier.size(16.dp))
                                content(item.second)
                            }
                            is TreeStruct.TreeNode -> {
                                Image(if(expand(node)){
                                    Icons.chevronDown()
                                } else {
                                    Icons.chevronRight()
                                }, null, modifier = Modifier.size(16.dp))
                                content(item.second)
                            }
                        }
                    }
                }
            }
        }

        if (withTreeHeader){
            val firstItemIndex by remember { derivedStateOf { state.firstVisibleItemIndex } }
            val headerItems = remember { mutableStateListOf<Pair<Int, TreeStruct.Node<T>>>() }
            LaunchedEffect(firstItemIndex,list){
                headerItems.clear()
                var indent = 0
                var item = list.getOrNull(firstItemIndex)
                while (item != null && item.first >= indent){
                    indent++
                    item = list.getOrNull(firstItemIndex + indent)
                }
                indent = (indent - 1).coerceAtLeast(0)
                var node = list.getOrNull(firstItemIndex + indent)
                if(node != null){
                    while(node!!.first != 0){
                        node = Pair(node.first - 1, node.second.parent!!)
                        headerItems.add(0,node)
                    }
                    while (headerItems.size > indent){
                        headerItems.removeLast()
                    }
                }


            }
            Column(Modifier.fillMaxWidth().padding(end = LocalScrollbarStyle.current.thickness)
                .align(Alignment.TopCenter).background(MaterialTheme.colorScheme.surface)
            ) {
                headerItems.forEach { item ->
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable { onClick(item.second) }.drawBehind {
                                repeat(item.first){
                                    drawRect(
                                        Color.hsv(((it * 40)%360).toFloat() ,1f,0.5f),
                                        topLeft = Offset(density.density * (it * 12 + 7),0f),
                                        size = Size(density.density * 2,size.height)
                                    )
                                }
                            }.padding(start = (12*item.first).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(Icons.chevronDown(), "null", modifier = Modifier.size(16.dp))
                        content(item.second)
                    }
                }
            }
        }
    }
}