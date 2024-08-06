package me.yricky.abcde.page

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import me.yricky.abcde.AppState
import me.yricky.abcde.HapSession
import me.yricky.abcde.content.ModuleInfoContent
import me.yricky.abcde.ui.*
import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.cfm.FieldType
import me.yricky.oh.abcd.cfm.MethodTag
import me.yricky.oh.abcd.code.Code
import me.yricky.oh.abcd.code.TryBlock
import me.yricky.oh.abcd.isa.*
import me.yricky.oh.abcd.isa.util.ExternModuleParser

class CodeView(val code: Code,override val hap:HapView? = null):AttachHapPage() {
    companion object{
        const val ANNO_TAG = "ANNO_TAG"
        const val ANNO_ASM_NAME = "ANNO_ASM_NAME"

        val operandParser = listOf(ExternModuleParser)
    }
    override val navString: String = "${hap?.navString ?: ""}${asNavString("ASM", code.method.defineStr(true))}"
    override val name: String = "${hap?.name ?: ""}/${code.method.abc.tag}/${code.method.clazz.name}/${code.method.name}"

    private val asmViewInfo:List<Pair<Asm.AsmItem, AnnotatedString>> by lazy{
        code.asm.list.map {
            buildAnnotatedString {
                append(buildAnnotatedString {
                    val asmName = it.asmName
                    append(asmName)
                    addStyle(
                        SpanStyle(Color(0xff9876aa)),
                        0,
                        asmName.length
                    )
                    addStringAnnotation(ANNO_TAG, ANNO_ASM_NAME,0, asmName.length)
                })
                append(' ')
                append(buildAnnotatedString {
                    it.asmArgs(operandParser).forEach { (index,argString) ->
                        if(argString != null) {
                            append(buildAnnotatedString {
                                append(argString)
                                addStringAnnotation(ANNO_TAG, "$index",0, argString.length)
                            })
                            append(' ')
                        }
                    }
                })
                append("    ")
                append(buildAnnotatedString {
                    val asmComment = it.asmComment
                    append(asmComment)
                    addStyle(
                        SpanStyle(commentColor),
                        0,
                        asmComment.length
                    )
                    addStringAnnotation(ANNO_TAG,"comment",0,asmComment.length)
                })
            }.let{ s -> Pair(it,s)}
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Page(modifier: Modifier, hapSession: HapSession, appState: AppState) {
        val clipboardManager = LocalClipboardManager.current
        VerticalTabAndContent(modifier, listOfNotNull(
            composeSelectContent { _: Boolean ->
                Image(Icons.asm(), null, Modifier.fillMaxSize())
            } to composeContent {
                Column(Modifier.fillMaxSize()) {
                    Text(
                        "寄存器数量:${code.numVRegs}, 参数数量:${code.numArgs}, 指令字节数:${code.codeSize}, TryCatch数:${code.triesSize}",
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Box(
                        Modifier.fillMaxWidth().weight(1f).padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(2.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        MultiNodeSelectionContainer {
                            var tryBlock by remember {
                                mutableStateOf<TryBlock?>(null)
                            }
                            val range = multiNodeSelectionState.rememberSelectionChange()
                            LaunchedEffect(null){
                                textActionFlow.collectLatest { when(it){
                                    is TextAction.Click -> {
                                        asmViewInfo.getOrNull(it.location.index)?.second
                                            ?.getStringAnnotations(ANNO_TAG,it.location.offset,it.location.offset + 1)
                                            ?.firstOrNull()
                                            ?.let { anno ->
                                                multiNodeSelectionState.selectedFrom = MultiNodeSelectionState.SelectionBound.from(it.location.index,anno.start)
                                                multiNodeSelectionState.selectedTo = MultiNodeSelectionState.SelectionBound.from(it.location.index,anno.end)
                                            }
                                    }
                                    is TextAction.DoubleClick -> {
                                        asmViewInfo.getOrNull(it.location.index)?.second?.let { str ->
                                            multiNodeSelectionState.selectedFrom = MultiNodeSelectionState.SelectionBound.from(it.location.index,0)
                                            multiNodeSelectionState.selectedTo = MultiNodeSelectionState.SelectionBound.from(it.location.index,str.length)
                                        }
                                    }
                                    is TextAction.Copy -> {
                                        clipboardManager.setText(buildAnnotatedString {
                                            asmViewInfo.forEachIndexed { index, (_,asmStr) ->
                                                it.range.rangeOf(index,asmStr)?.let {  r ->
                                                    append(asmStr.subSequence(r.start,r.endExclusive))
                                                    append('\n')
                                                }
                                            }
                                        })
                                    }
                                    is TextAction.SelectAll -> {
                                        multiNodeSelectionState.selectedFrom = MultiNodeSelectionState.SelectionBound.Zero
                                        multiNodeSelectionState.selectedTo = MultiNodeSelectionState.SelectionBound.from(asmViewInfo.size,asmViewInfo.lastOrNull()?.second?.length ?: 0)
                                    }
                                    else -> { }
                                } }
                            }
                            LazyColumnWithScrollBar {
                                item {
                                    Text(code.method.defineStr(true), style = codeStyle)
                                }
                                itemsIndexed(asmViewInfo) { index, (item,asmStr) ->
                                    Row {
                                        val line = remember {
                                            "$index ".let {
                                                "${" ".repeat((5 - it.length).coerceAtLeast(0))}$it"
                                            }
                                        }
                                        Text(line, style = codeStyle)
                                        val tb = remember(item) { item.tryBlocks }
                                        ContextMenuArea(
                                            items = {
                                                buildList<ContextMenuItem> {
                                                    if (tryBlock != null) {
                                                        add(ContextMenuItem("隐藏行高亮") {
                                                            tryBlock = null
                                                        })
                                                    }
                                                    code.tryBlocks.forEach {
                                                        add(
                                                            ContextMenuItem(
                                                                "高亮 TryBlock[0x${
                                                                    it.startPc.toString(
                                                                        16
                                                                    )
                                                                },0x${(it.startPc + it.length).toString(16)}]"
                                                            ) {
                                                                tryBlock = it
                                                            }
                                                        )
                                                    }
                                                    item.calledMethods.forEach {
                                                        add(ContextMenuItem("跳转到${it.name}"){
                                                            hapSession.openCode(hap,it)
                                                        })
                                                    }
                                                }
                                            }
                                        ) {
                                            Text(
                                                String.format("%04X ", item.codeOffset),
                                                style = codeStyle.copy(color = commentColor),
                                                modifier = with(Modifier) {
                                                    val density = LocalDensity.current
                                                    if (tryBlock != null) {
                                                        drawBehind {
                                                            if (tb.contains(tryBlock)) {
                                                                drawRect(
                                                                    Color.Yellow,
                                                                    size = Size(density.density * 2, size.height)
                                                                )
                                                            }
                                                        }
                                                    } else this
                                                }.let { m ->
                                                    if (tryBlock?.catchBlocks?.find { item.codeOffset in (it.handlerPc until (it.handlerPc + it.codeSize)) } != null) {
                                                        m.background(MaterialTheme.colorScheme.errorContainer)
                                                    } else {
                                                        m
                                                    }
                                                }
                                            )
                                        }
                                        TooltipArea(tooltip = {
                                            Surface(
                                                shape = MaterialTheme.shapes.medium,
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                InstInfo(Modifier.padding(8.dp),item.ins)
                                            }
                                        }, modifier = Modifier.fillMaxSize()) {
                                            val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
                                            val selectColor = LocalTextSelectionColors.current.backgroundColor
                                            val thisRange = range?.rangeOf(index,asmStr)
                                            Text(
                                                text = remember(thisRange) {
                                                    if(thisRange == null || thisRange.isEmpty()){
                                                        asmStr
                                                    } else {
                                                        val sp = asmStr.spanStyles.plus(
                                                            AnnotatedString.Range(
                                                                SpanStyle(background = selectColor),
                                                                thisRange.start,
                                                                thisRange.endExclusive,
                                                            )
                                                        )
                                                        AnnotatedString(
                                                            asmStr.text,
                                                            sp,
                                                            asmStr.paragraphStyles,
                                                        )
                                                    }
                                                },
                                                style = codeStyle,
                                                modifier = Modifier
                                                    .withMultiNodeSelection({ layoutResult.value },index)
                                                    .fillMaxWidth(),
                                                onTextLayout = { layoutResult.value = it },
                                            )
                                            Text("\n", maxLines = 1, style = codeStyle)
                                        }
                                    }
                                }
                                item {
                                    Spacer(Modifier.height(120.dp))
                                }
                            }
                        }
                        FloatingActionButton({
                            clipboardManager.setText(AnnotatedString(asmViewInfo.fold("") { s, i ->
                                "$s\n${i.second}"
                            }))
                        }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                            Text("复制")
                        }
                    }

                }
            }, composeSelectContent { _: Boolean ->
                Image(Icons.listFiles(), null, Modifier.fillMaxSize().alpha(0.5f), colorFilter = grayColorFilter)
            } to composeContent {
                Column(Modifier.fillMaxSize()) {
                    LazyColumnWithScrollBar {
                        items(code.method.data) {
                            when(it){
                                is MethodTag.DbgInfo -> Column {
                                    Text("params:${it.info.params}")
                                    Text("constantPool:${it.info.constantPool}")
                                    Text("$it")
                                }
                                else -> Text("$it")
                            }
                        }
                    }
                }
            }, (code.method.clazz as? FieldType.ClassType)?.let { it.clazz as? AbcClass }?.let { clazz ->
                composeSelectContent{ _:Boolean ->
                    Image(Icons.pkg(), null, Modifier.fillMaxSize().alpha(0.5f), colorFilter = grayColorFilter)
                } to composeContent{
                    ModuleInfoContent(Modifier.fillMaxSize(),clazz)
                }
            }
        ))
    }
}


