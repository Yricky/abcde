package me.yricky.abcde.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonPrimitive
import me.yricky.abcde.util.TreeModel
import me.yricky.oh.common.TreeStruct

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JsonTree(
    modifier: Modifier = Modifier,
    json:TreeModel<JsonPrimitive>
){

    val list = remember(json) { json.buildFlattenList { true } }
    TreeItemList(
        modifier = modifier,
        list = list,
        expand = { true },
        withTreeHeader = false
    ){
        when (val node = it) {
            is TreeStruct.LeafNode<JsonPrimitive> -> {
                FlowRow{
                    Image(Icons.info(), null, modifier = Modifier.padding(end = 2.dp).size(18.dp))
                    Text("${it.pathSeg}: ", maxLines = 1, overflow = TextOverflow.Ellipsis, style = codeStyle)
                    SelectionContainer {
                        Text(node.value.toString().replace("\\n","\n"), style = codeStyle)
                    }
                }
            }
            is TreeStruct.TreeNode<JsonPrimitive> -> {
                Image(Icons.pkg(), null, modifier = Modifier.padding(end = 2.dp).size(20.dp))
                Text(it.pathSeg, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}