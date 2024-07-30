package me.yricky.abcde.content

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.yricky.abcde.desktop.DesktopUtils
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
        Text("-----")
        Text("应用信息")
        Text("渲染Api：${DesktopUtils.AppStatus.renderApi}",style = MaterialTheme.typography.bodySmall)
        Text("临时目录：${DesktopUtils.tmpDir.absolutePath}",style = MaterialTheme.typography.bodySmall)
        Text("数据目录：${DesktopUtils.dataDir.absolutePath}",style = MaterialTheme.typography.bodySmall)
    }
}