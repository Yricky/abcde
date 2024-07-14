package me.yricky.abcde.page

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yricky.abcde.AppState

sealed class Page{
    companion object{
        fun asNavString(prefix:String,str:String) = "$prefix${String.format("%04d",str.length)}${str}"
    }

    abstract val navString:String
    abstract val name:String

    override fun equals(other: Any?): Boolean {
        if(javaClass != other?.javaClass){
            return false
        }
        return navString == (other as Page).navString
    }

    override fun hashCode(): Int {
        return navString.hashCode()
    }

    @Composable
    abstract fun Page(modifier: Modifier, appState: AppState)

}

sealed class AttachHapPage:Page(){
    abstract var hap:HapView?
        protected set

    fun attachHap(hap:HapView){
        if(this.hap == null){
            this.hap = hap
        }
    }
}