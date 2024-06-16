package me.yricky.abcde.page

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import me.yricky.abcde.ui.*
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.code.Code

val CODE_FONT = FontFamily(Font("fonts/jbMono/JetBrainsMono-Regular.ttf"))
val commentColor = Color(0xff72737a)
val codeStyle @Composable get() = TextStyle(
    fontFamily = CODE_FONT,
    color = Color(0xffa9b7c6),
    fontSize = MaterialTheme.typography.bodyMedium.fontSize
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CodeViewPage(modifier: Modifier, method: AbcMethod, code: Code?) {
    VerticalTabAndContent(modifier, listOf(
        code?.let {
            composeSelectContent{ _:Boolean ->
                Image(Icons.asm(), null, Modifier.fillMaxSize())
            } to composeContent{
                Column(Modifier.fillMaxSize()) {

                    Text("寄存器数量:${code.numVRegs}, 参数数量:${code.numArgs}, 指令字节数:${code.codeSize}",modifier = Modifier.padding(horizontal = 4.dp))
                    CompositionLocalProvider(
                        LocalScrollbarStyle provides LocalScrollbarStyle.current.copy(
                            unhoverColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            hoverColor = MaterialTheme.colorScheme.tertiary
                        )
                    ){
                        Box(Modifier.fillMaxWidth().weight(1f).padding(4.dp)
                            .border(2.dp,MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                        ) {
                            SelectionContainer {
                                LazyColumnWithScrollBar {
                                    item {
                                        Text(method.defineStr(true), style = codeStyle)
                                    }
                                    itemsIndexed(code.asm.list){index,it ->
                                        Row {
                                            DisableSelection {
                                                val line = remember {
                                                    "$index ".let {
                                                        "${" ".repeat((5 - it.length).coerceAtLeast(0))}$it"
                                                    }
                                                }
                                                Text(line, style = codeStyle)
                                            }
                                            DisableSelection {
                                                Text(String.format("%04X ",it.codeOffset), style = codeStyle.copy(color = commentColor))
                                            }
                                            TooltipArea(tooltip = {
                                                DisableSelection {
                                                    Surface(shape = MaterialTheme.shapes.medium,
                                                        color = MaterialTheme.colorScheme.primaryContainer) {
                                                        Column(modifier = Modifier.padding(8.dp)) {
                                                            Text(it.ins.instruction.sig, style = codeStyle)
                                                            if(it.ins.instruction.properties != null){
                                                                Text("prop:${it.ins.instruction.properties}", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                                                            }
                                                            Text("组:${it.ins.group.title}", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                                                            Text("组描述:${it.ins.group.description.trim()}", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                                                        }
                                                    }
                                                }
                                            }, modifier = Modifier.fillMaxSize()){
                                                Text(text = buildAnnotatedString {
                                                        val asmLine = it.disassembleString
                                                        append(asmLine)
                                                        Regex("//.*$").findAll(asmLine).forEach {
                                                            addStyle(
                                                                SpanStyle(commentColor),
                                                                it.range.first,
                                                                it.range.last + 1
                                                            )
                                                        }
                                                        Regex("^\\S*\\s").findAll(asmLine).forEach { f ->
                                                            addStyle(
                                                                SpanStyle(Color(0xff9876aa)),
                                                                f.range.first,
                                                                f.range.last + 1
                                                            )
                                                        }
                                                    }, style = codeStyle, modifier = Modifier.fillMaxWidth()
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
                                clipboardManager.setText(AnnotatedString(it.asm.list.fold("\n"){ s,i ->
                                    "$s\n${i.disassembleString}"
                                }))
                            },modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)){
                                Text("复制")
                            }
                        }
                    }
                }
            }
        }, composeSelectContent{ _:Boolean ->
            Image(Icons.pkg(), null, Modifier.fillMaxSize().alpha(0.5f), colorFilter = grayColorFilter)
        } to composeContent{
            Column(Modifier.fillMaxSize()) {
                LazyColumnWithScrollBar {
                    items(method.data){
                        Text("$it")
                    }
                }
            }
        }
    ).filterNotNull())
}