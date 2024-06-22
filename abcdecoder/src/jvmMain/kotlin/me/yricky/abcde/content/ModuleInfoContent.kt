package me.yricky.abcde.content

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.yricky.abcde.ui.LazyColumnWithScrollBar
import me.yricky.abcde.ui.icon
import me.yricky.oh.abcd.cfm.AbcClass

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModuleInfoContent(
    modifier: Modifier,
    clazz: AbcClass
){
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(clazz.icon(), null, modifier = Modifier.padding(8.dp).size(24.dp))
            Text(clazz.name, style = MaterialTheme.typography.titleLarge)
        }
        LazyColumnWithScrollBar {
            clazz.moduleInfo?.let { m ->
                stickyHeader {
                    Text("ModuleRequests(${m.moduleRequestNum})",Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface))
                }
                items(m.moduleRequests){
                    Text("- $it")
                }
                stickyHeader {
                    Text("RegularImports(${m.regularImportNum})",Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface))
                }
                items(m.regularImports){
                    Column(Modifier.padding(4.dp)) {
                        Text("- ${it}")
                    }
                }
                stickyHeader {
                    Text("NamespaceImports(${m.namespaceImportNum})",Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface))
                }
                items(m.namespaceImports){
                    Column(Modifier.padding(4.dp)) {
                        Text("- localName:${it.localName}")
                        Text("- moduleRequest:${it.moduleRequest}")
                    }
                }
                stickyHeader {
                    Text("LocalExports(${m.localExportNum})",Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface))
                }
                items(m.localExports){
                    Column(Modifier.padding(4.dp)) {
                        Text("- ${it}")
                    }
                }
                stickyHeader {
                    Text("IndirectExports(${m.indirectExportNum})",Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface))
                }
                items(m.indirectExports){
                    Column(Modifier.padding(4.dp)) {
                        Text("- importName:${it.importName}")
                        Text("- exportName:${it.exportName}")
                        Text("- moduleRequest:${it.moduleRequest}")
                    }
                }
                stickyHeader {
                    Text("StarExports(${m.starExportNum})",Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface))
                }
                items(m.starExports){
                    Column {
                        Text("- moduleRequest:${it.moduleRequest}")
                    }
                }
            }
        }
    }
}