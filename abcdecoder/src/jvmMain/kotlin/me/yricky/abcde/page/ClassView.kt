package me.yricky.abcde.page

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.yricky.abcde.AppState
import me.yricky.abcde.HapSession
import me.yricky.abcde.content.ModuleInfoContent
import me.yricky.abcde.content.ScopeInfoTooltip
import me.yricky.abcde.ui.*
import me.yricky.abcde.util.TreeModel
import me.yricky.oh.abcd.cfm.*
import me.yricky.oh.common.TreeStruct
import kotlin.collections.map

class ClassView(val classItem: AbcClass,override val hap:HapSession):AttachHapPage() {
    override val navString: String = "${hap.hapView?.navString ?: ""}${asNavString("CLZ", classItem.name)}"
    override val name: String = "${hap.hapView?.name?:""}/${classItem.abc.tag}/${classItem.name}"

    private val sourceCodeString by lazy {
        classItem.entryFunction()?.debugInfo?.state?.sourceCodeString
    }

    private val tabState = mutableIntStateOf(0)

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Page(modifier: Modifier, hapSession: HapSession, appState: AppState) {
        VerticalTabAndContent(modifier, tabState, listOfNotNull(
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
                    var showFuncAsTree by remember { mutableStateOf(false) }
                    val filteredMethods: List<AbcMethod> = remember(methodFilter) {
                        classItem.methods.filter { it.name.contains(methodFilter) }
                    }
                    val filteredMethodTree: List<Pair<Int, TreeStruct.Node<AbcMethod>>> = remember(filteredMethods) {
                        TreeModel(
                            TreeStruct(filteredMethods.map {
                                Pair(it.scopeInfo?.asNameIterable(it)?: listOf(it.name),it)
                            })
                        ).buildFlattenList{ true }
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
//                                        maxLines = 1,
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
                                    //Checkbox(showFuncAsTree, { showFuncAsTree = it })
                                    //Text("展示树形结构")
                                }
                            }
                        }
                        if (showFuncAsTree){
                            treeItems(filteredMethodTree, { true }) { node ->
                                when(node){
                                    is TreeStruct.LeafNode<AbcMethod> -> {
                                        RowMethodItem(node.value, node.pathSeg, Modifier.clearFocusWhenEnter(focus)
                                            .fillMaxWidth().clickable { hapSession.openCode(node.value) })
                                    }
                                    is TreeStruct.TreeNode<AbcMethod> -> {
                                        Text(node.pathSeg, Modifier.clearFocusWhenEnter(focus))
                                    }
                                }
                            }
                        } else {
                            items(filteredMethods) {
                                val scopeInfo = remember(it) { AbcMethod.ScopeInfo.parseFromMethod(it) }
                                TooltipArea({
                                    scopeInfo?.let { i -> ScopeInfoTooltip(it,i) }
                                }){
                                    RowMethodItem(it, null,Modifier.clearFocusWhenEnter(focus)
                                        .fillMaxWidth().clickable { hapSession.openCode(it) })
                                }
                            }
                        }
                    }
                }
            },
            sourceCodeString?.let {
                composeSelectContent { _:Boolean ->
                    Image(Icons.xml(), null, Modifier.fillMaxSize().alpha(0.5f), colorFilter = grayColorFilter)
                } to composeContent {
                    Column(
                        Modifier.padding(horizontal = 8.dp).verticalScroll(rememberScrollState())
                    ) {
                        SelectionContainer {
                            Text(it, style = codeStyle)
                        }
                    }
                }
            }
            , composeSelectContent{ _:Boolean ->
                Image(Icons.pkg(), null, Modifier.fillMaxSize().alpha(0.5f), colorFilter = grayColorFilter)
            } to composeContent{
                ModuleInfoContent(Modifier.fillMaxSize(),classItem)
            }
        ))
    }
}