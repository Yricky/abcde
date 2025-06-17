package me.yricky.abcde.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp

@Composable
fun SearchText(
    value:String,
    modifier: Modifier = Modifier,
    onValueChange:(String)->Unit
){
    Row(
        modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .height(20.dp)
            .padding(end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(Icons.search() ,null, modifier = Modifier.aspectRatio(1f).padding(4.dp))
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.replace(" ", "").replace("\n", "")) },
            textStyle = MaterialTheme.typography.labelMedium.merge(color = MaterialTheme.colorScheme.onSecondaryContainer),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSecondaryContainer),
        )
    }
}

@Composable
fun CheckedLabel(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit,
){
    Row(
        Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .height(20.dp)
            .clickable{
                onCheckedChange(!checked)
            }
            .padding(end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.aspectRatio(1f)
            .padding(4.dp).clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .background(if(checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}