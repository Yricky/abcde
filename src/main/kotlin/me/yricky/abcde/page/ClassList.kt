package me.yricky.abcde.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.yricky.abcde.AppState
import me.yricky.abcde.ui.*
import me.yricky.oh.abcd.cfm.ClassItem
import me.yricky.oh.abcd.cfm.AbcClass

@Composable
fun ClassListPage(
    modifier: Modifier,
    appState: AppState,
    classList: AppState.ClassList
) {

    val scope = rememberCoroutineScope()
    VerticalTabAndContent(modifier, listOf(composeSelectContent{ _: Boolean ->
        Image(Icons.clazz(), null, Modifier.fillMaxSize(), colorFilter = grayColorFilter)
    } to composeContent{
        Column(Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = classList.filter,
                onValueChange = { _filter ->
                    val filter = _filter.replace(" ", "").replace("\n", "")
                    if (classList.filter != filter) {
                        classList.filter = filter
                        scope.launch {
                            if (classList.classList.isNotEmpty()) {
                                delay(500)
                            }
                            if (classList.filter == filter) {
                                classList.classList = classList.classMap.asSequence()
                                    .filter { it.value.name.contains(filter) }
                                    .map { it.value }.toList()
                            }
                        }
                    }
                },
                leadingIcon = {
                    Image(Icons.search(), null)
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("${classList.classList.size}个类")
                },
            )
            ClassList(Modifier.fillMaxWidth().weight(1f), classList.classList) {
                if (it is AbcClass) {
                    appState.openClass(it)
                }
            }
        }
    },
//        composeSelectContent{
//        Image(Icons.listFiles(), null, Modifier.fillMaxSize().alpha(0.5f), colorFilter = grayColorFilter)
//    } to composeContent{
//        Column {
//            OutlinedTextField(
//                value = "",
//                onValueChange = {
//
//                },
//                leadingIcon = {
//                    Image(Icons.search(), null)
//                },
//                modifier = Modifier.fillMaxWidth(),
//                label = {
//                    Text("${appState.abc.literalArrays.size}个字面数组")
//                },
//            )
//        }
//    }
    ))

}

@Composable
fun ClassList(
    modifier: Modifier,
    classList: List<ClassItem>,
    onClick: (ClassItem) -> Unit = {}
) {
    LazyColumnWithScrollBar(
        modifier
    ) {
        items(classList) { clazz ->
            Row(
                Modifier.fillMaxWidth()
                    .clickable { onClick(clazz) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(clazz.icon(), null)
                Text(clazz.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}