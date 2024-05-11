package me.yricky.abcde.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import me.yricky.abcde.ui.defineStr
import me.yricky.abcde.ui.requestFocusWhenEnter
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.code.Code

@Composable
fun CodeViewPage(modifier: Modifier,method: AbcMethod, code: Code){
    Column(modifier) {
        SelectionContainer {
            Text(method.defineStr())
        }
        OutlinedTextField(code.asm,{},
            label = {
                Text("寄存器数量：${code.numVRegs}, 参数数量：${code.numArgs}, 指令字节数：${code.codeSize}")
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
                        addStyle(SpanStyle(Color(0xff72737a)),it.range.first,it.range.last+1)
                    }
                    Regex("^\\S*\\s").findAll(it.text).forEach { f ->
                        addStyle(SpanStyle(Color(0xff9876aa)),f.range.first,f.range.last+1)
                    }
                    Regex("\n\\S*\\s").findAll(it.text).forEach { f ->
                        addStyle(SpanStyle(Color(0xff9876aa)),f.range.first,f.range.last+1)
                    }
                }, OffsetMapping.Identity)
            }
        )
//        HexView(Modifier.fillMaxWidth().weight(1f).background(MaterialTheme.colorScheme.primaryContainer), remember { HexViewState(code.instructions) })
    }
}