package me.yricky.abcde.page

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.yricky.abcde.AppState
import me.yricky.abcde.HapSession
import me.yricky.abcde.content.ModuleInfoContent
import me.yricky.abcde.ui.*
import me.yricky.oh.abcd.cfm.AbcField
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.cfm.isModuleRecordIdx

class ClassView(val classItem: AbcClass,override val hap:HapView? = null):AttachHapPage() {
    override val navString: String = "${hap?.navString ?: ""}${asNavString("CLZ", classItem.name)}"
    override val name: String = "${hap?.name?:""}/${classItem.abc.tag}/${classItem.name}"
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Page(modifier: Modifier, hapSession: HapSession, appState: AppState) {
        VerticalTabAndContent(modifier, listOf(
            composeSelectContent{ _:Boolean ->
                Image(classItem.icon(), null, Modifier.fillMaxSize(), colorFilter = grayColorFilter)
            } to composeContent{
                Column(Modifier.fillMaxSize()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(classItem.icon(), null, modifier = Modifier.padding(8.dp).size(24.dp))
                        Text(classItem.name, style = MaterialTheme.typography.titleLarge)
                    }
                    var fieldFilter by remember {
                        mutableStateOf("")
                    }
                    val filteredFields: List<AbcField> = remember(fieldFilter) {
                        classItem.fields.filter { it.name.contains(fieldFilter) }
                    }
                    var methodFilter by remember {
                        mutableStateOf("")
                    }
                    val filteredMethods: List<AbcMethod> = remember(methodFilter) {
                        classItem.methods.filter { it.name.contains(methodFilter) }
                    }
                    val focus = LocalFocusManager.current
                    LazyColumnWithScrollBar {
                        stickyHeader {
                            Surface(Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${classItem.numFields}个字段")
                                    SearchText(
                                        value = fieldFilter,
                                        onValueChange = { fieldFilter = it.replace(" ", "").replace("\n", "") },
                                    )
                                }
                            }
                        }
                        items(filteredFields) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clearFocusWhenEnter(focus).fillMaxWidth()
                            ) {
                                Image(it.icon(), null)
                                if (it.accessFlags.isEnum) {
                                    Image(Icons.enum(), null)
                                }
                                if (it.isModuleRecordIdx()) {
                                    Image(Icons.pkg(), null, modifier = Modifier.clickable {

                                    })
                                }
                                SelectionContainer {
                                    Text(
                                        it.defineStr(),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 0.sp
                                    )
                                }
                            }
                        }
                        stickyHeader {
                            Surface(Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${classItem.numMethods}个方法")
                                    SearchText(
                                        value = methodFilter,
                                        onValueChange = { methodFilter = it.replace(" ", "").replace("\n", "") },
                                    )
                                }
                            }
                        }
                        items(filteredMethods) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clearFocusWhenEnter(focus)
                                    .fillMaxWidth().clickable { hapSession.openCode(hap,it) }
                            ) {
                                Image(it.icon(), null)
                                it.codeItem?.let { c ->
                                    Image(Icons.watch(), null)
                                }
                                Text(
                                    it.defineStr(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 0.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }, composeSelectContent{ _:Boolean ->
                Image(Icons.pkg(), null, Modifier.fillMaxSize().alpha(0.5f), colorFilter = grayColorFilter)
            } to composeContent{
                ModuleInfoContent(Modifier.fillMaxSize(),classItem)
            }
        ))
    }
}