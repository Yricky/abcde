package me.yricky.abcde.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yricky.oh.abcd.isa.Inst

@Composable
fun InstInfo(
    modifier: Modifier = Modifier,
    ins:Inst
){
    Column(modifier = modifier) {
        Text(ins.instruction.sig, style = codeStyle)
        if (ins.instruction.properties != null) {
            Text(
                "prop:${ins.instruction.properties}",
                fontSize = MaterialTheme.typography.bodyMedium.fontSize
            )
        }
        Text(
            "组:${ins.group.title}",
            fontSize = MaterialTheme.typography.bodyMedium.fontSize
        )
        Text(
            "组描述:${ins.group.description.trim()}",
            fontSize = MaterialTheme.typography.bodyMedium.fontSize
        )
    }
}