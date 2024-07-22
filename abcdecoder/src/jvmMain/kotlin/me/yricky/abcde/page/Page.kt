package me.yricky.abcde.page

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.yricky.abcde.AppState
import me.yricky.abcde.HapSession

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
    abstract fun Page(modifier: Modifier, hapSession: HapSession, appState: AppState)

}

sealed class AttachHapPage:Page(){
    abstract val hap:HapView?
}

val Page.shortName get() = name.let {
    if (it.length > 35) "...${
        it.substring(
            it.length - 32,
            it.length
        )
    }" else it
}