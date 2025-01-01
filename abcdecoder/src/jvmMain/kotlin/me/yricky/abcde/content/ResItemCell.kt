package me.yricky.abcde.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import me.yricky.abcde.HapSession
import me.yricky.oh.resde.ResType

@Composable
fun ResItemCell(
    modifier: Modifier = Modifier,
    hap: HapSession,
    currKey: ResType?,
    data: String?
){
    Row(Modifier.width(240.dp).then(modifier)
    ) { data?.let { txt ->
        var namePop by remember { mutableStateOf(false) }
        Text(
            txt,
            modifier = Modifier.fillMaxHeight().weight(1f).clickable { namePop = !namePop },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if(currKey == ResType.MEDIA && hap.hapConfig!= null){
            val prefix = remember(hap.hapConfig) { "${hap.hapConfig.module.name}/" }
            if(txt.startsWith(prefix)){
                hap.loadPainterInZip(txt.removePrefix(prefix))?.let {
                    Image(it, null,Modifier.aspectRatio(1f).padding(1.dp))
                }
            }
        }
        if(namePop) Popup(
            onDismissRequest = { namePop = false },
            properties = PopupProperties(focusable = true)
        ) {
            BasicTextField(txt, {},Modifier.width(240.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
                textStyle = LocalTextStyle.current,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )
        }
    }}
}