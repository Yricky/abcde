package me.yricky.abcde.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor

@Composable
fun SearchText(
    value:String,
    modifier: Modifier = Modifier,
    onValueChange:(String)->Unit
){
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Image(Icons.search(), null)
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.replace(" ", "").replace("\n", "")) },
            textStyle = MaterialTheme.typography.bodyMedium.merge(color = MaterialTheme.colorScheme.onSecondaryContainer),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSecondaryContainer)
        )
    }
}