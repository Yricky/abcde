package me.yricky.abcde.page

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yricky.abcde.AppState

sealed class Page{
    abstract val tag:PageTag

    @Composable
    abstract fun Page(modifier: Modifier, appState: AppState)

    override fun equals(other: Any?): Boolean {
        if(other == null){
            return false
        }
        if(javaClass != other::class.java){
            return false
        }
        return tag == (other as Page).tag
    }

    override fun hashCode(): Int {
        var result = tag.hashCode()
        result = 31 * result + javaClass.hashCode()
        return result
    }

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