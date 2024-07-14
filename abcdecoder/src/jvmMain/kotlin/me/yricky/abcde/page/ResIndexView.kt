package me.yricky.abcde.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import me.yricky.abcde.AppState
import me.yricky.abcde.ui.LazyColumnWithScrollBar
import me.yricky.oh.resde.ResIndexBuf

class ResIndexView(val res:ResIndexBuf, name: String,override var hap:HapView? = null):AttachHapPage() {
    override val navString: String = "${hap?.navString ?: ""}${asNavString("REI", name)}"
    override val name: String = if(hap == null){ name } else "${hap?.name ?: ""}/$name"
    @Composable
    override fun Page(modifier: Modifier, appState: AppState) {
        Column {
            var filter:String by remember {
                mutableStateOf("")
            }
            OutlinedTextField(filter,{filter = it})
            val list = remember(filter) { res.resMap.entries.toList().filter { it.value.any { it.fileName.contains(filter) } } }

            LazyColumnWithScrollBar(modifier) {
                items(list) { e ->
                    Column {
                        if(e.value.all { it.fileName == e.value.first().fileName && it.resType == e.value.first().resType }){
                            Text("${e.value.firstOrNull()?.fileName} type:${e.value.firstOrNull()?.resType} id:${e.key}")
                            e.value.forEach {
                                Text("  ${it.limitKey} \"${it.data}\"", style = codeStyle)
                            }
                        } else {
                            Text("id:${e.key}")
                            e.value.forEach {
                                Text("  ${it.fileName} ${it.resType} ${it.limitKey} \"${it.data}\"", style = codeStyle)
                            }
                        }
                    }
                }
            }
        }

    }
}