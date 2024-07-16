package me.yricky.abcde.content

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yricky.oh.abcd.isa.Asm

@Composable
fun VersionPanel(
    modifier: Modifier = Modifier
){
    Column(modifier) {
        Text("方舟字节码版本")
        Text("当前：${Asm.innerAsmMap.isa.version}",style = MaterialTheme.typography.bodySmall)
        Text("最低：${Asm.innerAsmMap.isa.minVersion}",style = MaterialTheme.typography.bodySmall)
        Text("-----")
        Text("Java信息")
        Text("版本：${System.getProperty("java.version")}",style = MaterialTheme.typography.bodySmall)
        Text("供应方：${System.getProperty("java.vendor")}",style = MaterialTheme.typography.bodySmall)
        Text("-----")
        Text("系统信息")
        Text("名称：${System.getProperty("os.name")}",style = MaterialTheme.typography.bodySmall)
        Text("版本：${System.getProperty("os.version")}",style = MaterialTheme.typography.bodySmall)
        Text("架构：${System.getProperty("os.arch")}",style = MaterialTheme.typography.bodySmall)
    }
}