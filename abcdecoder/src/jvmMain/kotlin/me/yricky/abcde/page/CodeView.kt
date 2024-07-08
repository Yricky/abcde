package me.yricky.abcde.page

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import me.yricky.abcde.AppState
import me.yricky.abcde.content.ModuleInfoContent
import me.yricky.abcde.ui.*
import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.cfm.FieldType
import me.yricky.oh.abcd.code.Code
import me.yricky.oh.abcd.code.TryBlock
import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.abcd.isa.calledMethods

class CodeView(val method: AbcMethod, val code: Code? = method.codeItem):Page() {
    override val tag: String = method.name

    @Composable
    override fun Page(modifier: Modifier, appState: AppState) {
        CodeViewPage(modifier, appState, method, code)
    }

    override fun equals(other: Any?): Boolean {
        if(other !is CodeView){
            return false
        }
        return method == other.method
    }

    override fun hashCode(): Int {
        return method.hashCode()
    }
}


val CODE_FONT = FontFamily(Font("fonts/jbMono/JetBrainsMono-Regular.ttf"))
val commentColor = Color(0xff72737a)
val codeStyle @Composable get() = TextStyle(
    fontFamily = CODE_FONT,
    color = Color(0xffa9b7c6),
    fontSize = MaterialTheme.typography.bodyMedium.fontSize
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CodeViewPage(modifier: Modifier, appState: AppState, method: AbcMethod, code: Code?) {
    VerticalTabAndContent(modifier, listOfNotNull(
        code?.let {
            composeSelectContent { _: Boolean ->
                Image(Icons.asm(), null, Modifier.fillMaxSize())
            } to composeContent {
                Column(Modifier.fillMaxSize()) {
                    Text(
                        "寄存器数量:${code.numVRegs}, 参数数量:${code.numArgs}, 指令字节数:${code.codeSize}, TryCatch数:${code.triesSize}",
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    val asmString:Map<Asm.AsmItem, AnnotatedString> = remember {
                        code.asm.list.associateWith {
                            buildAnnotatedString {
                                append(buildAnnotatedString {
                                    val asmName = it.asmName
                                    append(asmName)
                                    addStyle(
                                        SpanStyle(Color(0xff9876aa)),
                                        0,
                                        asmName.length
                                    )
                                })
                                append(' ')
                                append(buildAnnotatedString {
                                    append(it.asmArgs)
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
                                })
                            }
                        }
                    }
                    Box(
                        Modifier.fillMaxWidth().weight(1f).padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(2.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        FixedSelectionContainer {
                            var tryBlock by remember {
                                mutableStateOf<TryBlock?>(null)
                            }
                            LazyColumnWithScrollBar {
                                item {
                                    Text(method.defineStr(true), style = codeStyle)
                                }
                                itemsIndexed(code.asm.list) { index, item ->
                                    Row {
                                        item.asm
                                        DisableSelection {
                                            val line = remember {
                                                "$index ".let {
                                                    "${" ".repeat((5 - it.length).coerceAtLeast(0))}$it"
                                                }
                                            }
                                            Text(line, style = codeStyle)
                                        }
                                        DisableSelection {
                                            val tb = remember(item) { item.tryBlocks }
                                            ContextMenuArea(
                                                items = {
                                                    buildList<ContextMenuItem> {
                                                        if (tryBlock != null) {
                                                            add(ContextMenuItem("隐藏行高亮") {
                                                                tryBlock = null
                                                            })
                                                        }
                                                        item.asm
                                                        it.tryBlocks.forEach {
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
                                                                appState.openCode(it)
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
                                        }
                                        TooltipArea(tooltip = {
                                            DisableSelection {
                                                Surface(
                                                    shape = MaterialTheme.shapes.medium,
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp)) {
                                                        Text(item.ins.instruction.sig, style = codeStyle)
                                                        if (item.ins.instruction.properties != null) {
                                                            Text(
                                                                "prop:${item.ins.instruction.properties}",
                                                                fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                                            )
                                                        }
                                                        Text(
                                                            "组:${item.ins.group.title}",
                                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                                        )
                                                        Text(
                                                            "组描述:${item.ins.group.description.trim()}",
                                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                                        )
                                                    }
                                                }
                                            }
                                        }, modifier = Modifier.fillMaxSize()) {
                                            Text(
                                                text = asmString[item]!!, style = codeStyle, modifier = Modifier.fillMaxWidth()
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
                        val clipboardManager = LocalClipboardManager.current
                        FloatingActionButton({
                            clipboardManager.setText(AnnotatedString(asmString.values.fold("\n") { s, i ->
                                "$s\n${i}"
                            }))
                        }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                            Text("复制")
                        }
                    }

                }
            }
        }, composeSelectContent { _: Boolean ->
            Image(Icons.listFiles(), null, Modifier.fillMaxSize().alpha(0.5f), colorFilter = grayColorFilter)
        } to composeContent {
            Column(Modifier.fillMaxSize()) {
                LazyColumnWithScrollBar {
                    items(method.data) {
                        Text("$it")
                    }
                }
            }
        }, (method.clazz as? FieldType.ClassType)?.let { it.clazz as? AbcClass }?.let { clazz ->
            composeSelectContent{ _:Boolean ->
                Image(Icons.pkg(), null, Modifier.fillMaxSize().alpha(0.5f), colorFilter = grayColorFilter)
            } to composeContent{
                ModuleInfoContent(Modifier.fillMaxSize(),clazz)
            }
        }
    ))
}