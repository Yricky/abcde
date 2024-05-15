package me.yricky.abcde.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import me.yricky.abcde.ui.*
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.code.Code

@Composable
fun CodeViewPage(modifier: Modifier, method: AbcMethod, code: Code?) {
    VerticalTabAndContent(modifier, listOf(
        code?.let {
            composeSelectContent{ _:Boolean ->
                Image(Icons.asm(), null, Modifier.fillMaxSize())
            } to composeContent{
                Column(Modifier.fillMaxSize()) {
                    val asmString = remember {
                        code.asm.list.fold("${method.defineStr(true)}\n\n") { s1, s2 -> "$s1\n${code.asm.asmString(s2)}" }
                    }
                    OutlinedTextField("$asmString\n", {},
                        label = {
                            Text("寄存器数量:${code.numVRegs}, 参数数量:${code.numArgs}, 指令字节数:${code.codeSize}")
                        },
                        modifier = Modifier.fillMaxWidth().weight(1f).requestFocusWhenEnter(remember { FocusRequester() }),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xffa9b7c6),
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize
                        ),
                        visualTransformation = VisualTransformation {
                            TransformedText(buildAnnotatedString {
                                append(it.text)
                                Regex("//.*\n").findAll(it.text).forEach {
                                    addStyle(SpanStyle(Color(0xff72737a)), it.range.first, it.range.last + 1)
                                }

                                Regex("\n\\S*\\s").findAll(it.text).forEach { f ->
                                    addStyle(SpanStyle(Color(0xff9876aa)), f.range.first, f.range.last + 1)
                                }
                            }, OffsetMapping.Identity)
                        }
                    )
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