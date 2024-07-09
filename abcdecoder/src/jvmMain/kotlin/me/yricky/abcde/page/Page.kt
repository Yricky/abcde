package me.yricky.abcde.page

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yricky.abcde.AppState

sealed class Page{
    abstract val tag:String

    @Composable
    abstract fun Page(modifier: Modifier, appState: AppState)
}

sealed class AttachHapPage:Page(){
    var hap:HapView? = null
        private set

    fun attachHap(hap:HapView){
        if(this.hap == null){
            this.hap = hap
        }
    }
}