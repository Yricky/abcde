package me.yricky.abcde.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.yricky.abcde.ui.codeStyle
import me.yricky.oh.abcd.cfm.AbcMethod

@Composable
fun ScopeInfoTooltip(
    method: AbcMethod,
    scopeInfo: AbcMethod.ScopeInfo,
){
    CompositionLocalProvider(LocalTextStyle provides codeStyle){
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(Modifier.padding(8.dp)) {
                scopeInfo.layers.forEachIndexed { i, sl ->
                    Text( " ".repeat(i) + "$sl")
                }
                Text(" ".repeat(scopeInfo.layers.size) +
                        AbcMethod.ScopeInfo.decorateMethodName(method.name.removeRange(scopeInfo.origin),scopeInfo.tag))
            }
        }

    }
}