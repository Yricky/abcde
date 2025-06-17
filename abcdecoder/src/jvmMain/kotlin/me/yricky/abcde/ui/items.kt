package me.yricky.abcde.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import me.yricky.oh.abcd.cfm.AbcMethod
import me.yricky.oh.abcd.cfm.argsStr

@Composable
fun RowMethodItem(
    it: AbcMethod,
    name: String? = null,
    modifier: Modifier = Modifier,
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Image(Icons.method(), null)
//        it.codeItem?.let { c ->
//            Image(Icons.watch(), null)
//        }

//        val funcName = remember(it.name) {
//            buildAnnotatedString {
//                append(it.name)
//                AbcMethod.ScopeInfo.scopeRegex.find(it.name)?.let {
//                    it.groups[1]
//                }?.let { res ->
//                    addStyle(SpanStyle(
//                        textDecoration = TextDecoration.Underline
//                    ),res.range.first,res.range.last + 1)
//                }
//            }
//        }


        Text(
            name ?: it.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = codeStyle
        )
        Text(
            it.argsStr(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = codeStyle,
            modifier = Modifier.weight(1f)
        )
    }
}