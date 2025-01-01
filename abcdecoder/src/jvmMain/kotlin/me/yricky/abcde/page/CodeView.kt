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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.DpOffset
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
import me.yricky.oh.abcd.isa.util.ResParser
import me.yricky.oh.abcd.isa.util.V2AInstParser

class CodeView(val code: Code,override val hap:HapSession):AttachHapPage() {
    companion object{
        const val ANNO_TAG = "ANNO_TAG"
        const val ANNO_ASM_NAME = "ANNO_ASM_NAME"

        fun tryBlockString(tb:TryBlock):String = "TryBlock[0x${tb.startPc.toString(16)},0x${(tb.startPc + tb.length).toString(16)})"
    }
    val operandParser = listOfNotNull(hap.openedRes()?.let { ResParser(it) },V2AInstParser,ExternModuleParser)

    override val navString: String = "${hap.hapView?.navString ?: ""}${asNavString("ASM", code.method.defineStr(true))}"
    override val name: String = "${hap.hapView?.name ?: ""}/${code.method.abc.tag}/${code.method.clazz.name}/${code.method.name}"

    private fun genAsmViewInfo():List<Pair<Asm.AsmItem, AnnotatedString>> {
        return code.asm.list.map {
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

    private var asmViewInfo:List<Pair<Asm.AsmItem, AnnotatedString>> by mutableStateOf(genAsmViewInfo())
    private var showLabel:Boolean by mutableStateOf(false)

    private val tabState = mutableIntStateOf(0)

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Page(modifier: Modifier, hapSession: HapSession, appState: AppState) {
        val clipboardManager = LocalClipboardManager.current
        VerticalTabAndContent(modifier, tabState, listOfNotNull(
            composeSelectContent { _: Boolean ->
                Image(Icons.asm(), null, Modifier.fillMaxSize())
            } to composeContent {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp).padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when(val clz = code.method.clazz){
                            is FieldType.ClassType -> Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    hapSession.openClass(clz.clazz as AbcClass)
                                }
                            ){
                                Image(Icons.clazz(), null)
                                Text("返回所在的类")
                            }
                            else -> {}
                        }

                        Spacer(Modifier.weight(1f))
                        if(code.tryBlocks.isNotEmpty()){
                            Checkbox(showLabel,{ showLabel = it },Modifier.size(24.dp))
                            Text("展示TryCatch标签")
                        }
                    }
                    Box(
                        Modifier.fillMaxWidth().weight(1f).padding(end = 8.dp, bottom = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(2.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        MultiNodeSelectionContainer {
                            val range = multiNodeSelectionState.rememberSelectionChange()
                            var hovered: TextAction.Hover? by remember { mutableStateOf(null) }
                            //处理文本选择等操作
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
                                    is TextAction.Hover -> {
                                        hovered = it.takeIf { !it.location.invalid() }
                                    }
                                    else -> {  }
                                } }
                            }
                            //汇编代码展示
                            TooltipArea(tooltip = {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    hovered?.let {
                                        HoveredTooltip(it)
                                    }
                                }
                            }, tooltipPlacement = TooltipPlacement.CursorPoint(
                                offset = DpOffset(16.dp, 16.dp)
                            )) {
                                LazyColumnWithScrollBar {
                                    item {
                                        Text(
                                            "寄存器个数:${code.numVRegs}, 参数个数:${code.numArgs}, 指令字节数:${code.codeSize}, TryCatch数:${code.triesSize}",
                                            style = codeStyle
                                        )
                                    }
                                    item {
                                        Text(code.method.defineStr(true), style = codeStyle)
                                    }
                                    itemsIndexed(asmViewInfo) { index, (item, asmStr) ->
                                        Row {
                                            val line = remember {
                                                "$index ".let {
                                                    "${" ".repeat((5 - it.length).coerceAtLeast(0))}$it"
                                                }
                                            }
                                            Text(line, style = codeStyle)
                                            Text(
                                                String.format("%04X ", item.codeOffset),
                                                style = codeStyle.copy(color = commentColor)
                                            )

                                            val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
                                            val selectColor = LocalTextSelectionColors.current.backgroundColor
                                            val thisRange = range?.rangeOf(index, asmStr)
                                            Column(Modifier.fillMaxSize()) {
                                                if (showLabel) code.tryBlocks.asSequence().filter { it.length > 0 }
                                                    .forEach {
                                                        if (it.startPc == item.codeOffset) {
                                                            Text(
                                                                "${tryBlockString(it)}_begin",
                                                                style = MaterialTheme.typography.labelSmall
                                                            )
                                                        }
                                                        it.catchBlocks.forEach { cb ->
                                                            if (cb.handlerPc == item.codeOffset) {
                                                                Text(
                                                                    "${tryBlockString(it)}_catch${if (cb.codeSize > 0) "_begin" else ""}",
                                                                    style = MaterialTheme.typography.labelSmall
                                                                )
                                                            }
                                                        }
                                                    }
                                                Text(
                                                    text = remember(thisRange) {
                                                        if (thisRange == null || thisRange.isEmpty()) {
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
                                                        .withMultiNodeSelection({ layoutResult.value }, index)
                                                        .fillMaxWidth(),
                                                    onTextLayout = { layoutResult.value = it },
                                                )
                                                if (showLabel) code.tryBlocks.asSequence().filter { it.length > 0 }
                                                    .forEach {
                                                        it.catchBlocks.forEach { cb ->
                                                            if (cb.codeSize > 0 && cb.handlerPc + cb.codeSize == item.nextOffset) {
                                                                Text(
                                                                    "${tryBlockString(it)}_catch_end",
                                                                    style = MaterialTheme.typography.labelSmall
                                                                )
                                                            }
                                                        }
                                                        if (it.startPc + it.length == item.nextOffset) {
                                                            Text(
                                                                "${tryBlockString(it)}_end",
                                                                style = MaterialTheme.typography.labelSmall
                                                            )
                                                        }
                                                    }
                                            }
                                        }
                                    }
                                    item {
                                        Spacer(Modifier.height(120.dp))
                                    }
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

    @Composable
    fun HoveredTooltip(hovered: TextAction.Hover){
        asmViewInfo.getOrNull(hovered.location.index)?.let{ info ->
            info.second.getStringAnnotations(ANNO_TAG,hovered.location.offset,hovered.location.offset + 1)
                .firstOrNull()?.let { anno ->
                    when(anno.item){
                        ANNO_ASM_NAME -> InstInfo(Modifier.padding(8.dp),info.first.ins)
                        else -> {}
                    }
                }
        }
    }
}


