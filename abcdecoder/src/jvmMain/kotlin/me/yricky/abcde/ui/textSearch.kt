package me.yricky.abcde.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp

@Composable
fun TextSearchComponent(
    searchFocusRequester: FocusRequester,
    searchQuery:String,
    performSearch:(String)->Unit,
    previousSearchMatch:()->Unit,
    nextSearchMatch:()->Unit,
    currentSearchMatch:Int,
    searchMatchesCount:Int,
){
    Row(verticalAlignment = Alignment.CenterVertically) {
        SearchText(
            modifier = Modifier.focusRequester(searchFocusRequester),
            value = searchQuery,
            onValueChange = {
                performSearch(it)
            }
        )

        // 搜索结果计数和导航
        if (searchMatchesCount > 0) {

            Text(
                text = "${currentSearchMatch + 1}/$searchMatchesCount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Box(Modifier.size(24.dp)
                .clip(CircleShape)
                .clickable { previousSearchMatch() }){
                Text("▲",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodySmall)
            }
            Box(Modifier.size(24.dp)
                .clip(CircleShape)
                .clickable { nextSearchMatch() }){
                Text("▼",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodySmall)
            }
        } else if (searchQuery.isNotBlank()) {
            Text(
                text = "未找到匹配项",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}