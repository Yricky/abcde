package me.yricky.abcde.page

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.yricky.abcde.ui.*
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.code.Code
import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.abcd.isa.bean.asmName

@OptIn(ExperimentalFoundationApi::class)
val inlineContentMap = mutableMapOf<String,InlineTextContent>().also {
    Asm.asmMap.isa.groups.forEach { g ->
        g.instructions.forEach { i ->
            it.put(i.asmName(),InlineTextContent(Placeholder(16.sp,16.sp, PlaceholderVerticalAlign.TextCenter)){
                TooltipArea(tooltip = {
                    DisableSelection {
                        Surface(shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(i.sig, style = codeStyle)
                                if(i.properties != null){
                                    Text("prop:${i.properties}", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                                }
                                Text("组:${g.title}", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                                Text("组描述:${g.description.trim()}", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                            }
                        }
                    }
                }, modifier = Modifier.fillMaxSize()){
                    Image(painter = Icons.info(),null, modifier = Modifier.size(16.dp))
                }
            })
        }
    }
}

val CODE_FONT = FontFamily(Font("fonts/jbMono/JetBrainsMono-Regular.ttf"))
val codeStyle @Composable get() = TextStyle(
    fontFamily = CODE_FONT,
    color = Color(0xffa9b7c6),
    fontSize = MaterialTheme.typography.bodyMedium.fontSize
)

@Composable
fun CodeViewPage(modifier: Modifier, method: AbcMethod, code: Code?) {
    VerticalTabAndContent(modifier, listOf(
        code?.let {
            composeSelectContent{ _:Boolean ->
                Image(Icons.asm(), null, Modifier.fillMaxSize())
            } to composeContent{
                Column(Modifier.fillMaxSize()) {

                    Text("寄存器数量:${code.numVRegs}, 参数数量:${code.numArgs}, 指令字节数:${code.codeSize}")
                    CompositionLocalProvider(
                        LocalScrollbarStyle provides LocalScrollbarStyle.current.copy(
                            unhoverColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            hoverColor = MaterialTheme.colorScheme.tertiary
                        )
                    ){
                        Box(Modifier.fillMaxWidth().weight(1f)) {
                            val scroll = rememberScrollState()
                            SelectionContainer {
                                Text(
                                    remember {
                                        buildAnnotatedString {
                                            append("${method.defineStr(true)}\n\n")
                                            code.asm.list.forEach {
                                                val asmLine = code.asm.asmString(it)
                                                appendInlineContent(it.ins.asmName, " ")
                                                append(buildAnnotatedString {
                                                    append(asmLine)
                                                    Regex("//.*$").findAll(asmLine).forEach {
                                                        addStyle(
                                                            SpanStyle(Color(0xff72737a)),
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
                                                })
                                                append('\n')
                                            }
                                        }
                                    },
                                    modifier = Modifier.verticalScroll(scroll)
                                        .requestFocusWhenEnter(remember { FocusRequester() }),
                                    style = codeStyle,
                                    inlineContent = inlineContentMap
                                )
                            }
                            VerticalScrollbar(
                                rememberScrollbarAdapter(scroll),
                                Modifier.fillMaxHeight().align(Alignment.CenterEnd)
                            )
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