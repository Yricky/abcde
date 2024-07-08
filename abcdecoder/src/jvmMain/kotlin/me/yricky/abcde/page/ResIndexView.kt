package me.yricky.abcde.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.yricky.abcde.AppState
import me.yricky.abcde.ui.LazyColumnWithScrollBar
import me.yricky.oh.resde.ResIndexBuf

class ResIndexView(val res:ResIndexBuf, override val tag: String):Page() {

    @Composable
    override fun Page(modifier: Modifier, appState: AppState) {
        val list = remember { res.resMap.entries.toList() }
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