package me.yricky.abcde.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.yricky.abcde.AppState
import me.yricky.abcde.ui.*
import me.yricky.oh.common.TreeStruct
import me.yricky.oh.abcd.cfm.ClassItem
import me.yricky.oh.abcd.cfm.AbcClass

@Composable
fun AbcOverviewPage(
    modifier: Modifier,
    appState: AppState,
    abcOverview: AppState.AbcOverview
) {

    val scope = rememberCoroutineScope()
    VerticalTabAndContent(modifier, listOf(composeSelectContent{ _: Boolean ->
        Image(Icons.clazz(), null, Modifier.fillMaxSize(), colorFilter = grayColorFilter)
    } to composeContent{
        Column(Modifier.fillMaxSize()) {
            var filter by remember(abcOverview.filter) {
                mutableStateOf(abcOverview.filter)
            }
            OutlinedTextField(
                value = filter,
                onValueChange = { _filter ->
                    filter = _filter.replace(" ", "").replace("\n", "")
                    scope.launch {
                        if (abcOverview.classList.isNotEmpty()) {
                            delay(500)
                        }
                        if (_filter == filter) {
                            println("Set:$_filter")
                            abcOverview.setNewFilter(filter)
                        } else {
                            println("drop:${_filter}")
                        }
                    }
                },
                leadingIcon = {
                    Image(Icons.search(), null)
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("${abcOverview.classCount}个类")
                },
            )
            ClassList(Modifier.fillMaxWidth().weight(1f), abcOverview.classList) {
                if (it is TreeStruct.LeafNode) {
                    val clazz = it.value
                    if(clazz is AbcClass){
                        appState.openClass(clazz)
                    }
                } else if(it is TreeStruct.TreeNode){
                    abcOverview.toggleExpand(it)
                }
            }
        }
    }, composeSelectContent{
        Image(Icons.info(), null, Modifier.fillMaxSize(), colorFilter = grayColorFilter)
    } to composeContent{
        Column {
            Text(abcOverview.abc.tag, style = MaterialTheme.typography.titleLarge)
            Text("文件版本:${abcOverview.abc.header.version}")
            Text("size:${abcOverview.abc.header.fileSize}")
            Text("Class数量:${abcOverview.abc.header.numClasses}")
            Text("行号处理程序数量:${abcOverview.abc.header.numLnps}")
            Text("IndexRegion数量:${abcOverview.abc.header.numIndexRegions}")
        }
    }
    ))

}

@Composable
fun ClassList(
    modifier: Modifier,
    classList: List<Pair<Int, TreeStruct.Node<ClassItem>>>,
    onClick: (TreeStruct.Node<ClassItem>) -> Unit = {}
) {
    val state = rememberLazyListState()
    Box(modifier){
        LazyColumnWithScrollBar(
            Modifier.fillMaxSize(),
            state
        ) {
            items(classList) { clazz ->
                ClassListItem(clazz, onClick)
            }
        }
    }

}

@Composable
fun ClassListItem(
    clazz: Pair<Int, TreeStruct.Node<ClassItem>>,
    onClick: (TreeStruct.Node<ClassItem>) -> Unit
){
    val density = LocalDensity.current
    Row(
        Modifier.fillMaxWidth()
            .clickable { onClick(clazz.second) }.drawBehind {
                repeat(clazz.first){
                    drawRect(
                        Color.hsv(((it * 40)%360).toFloat() ,1f,0.5f),
                        topLeft = Offset(density.density * (it * 12 + 5),0f),
                        size = Size(density.density * 2,size.height)
                    )
                }
            }.padding(start = (12*clazz.first).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (val node = clazz.second) {
            is TreeStruct.LeafNode<ClassItem> -> {
                Image(node.value.icon(), null, modifier = Modifier.padding(end = 2.dp).size(16.dp))
            }
            is TreeStruct.TreeNode<ClassItem> -> {
                Image(Icons.pkg(), null, modifier = Modifier.padding(end = 2.dp).size(16.dp))
            }
        }
        Text(clazz.second.pathSeg, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}