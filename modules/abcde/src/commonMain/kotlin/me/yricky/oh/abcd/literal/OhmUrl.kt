package me.yricky.oh.abcd.literal
import kotlin.jvm.JvmInline

/**
 * [see this](https://gitee.com/openharmony/arkcompiler_ets_runtime/blob/master/ecmascript/module/module_path_helper.h)
 */
@JvmInline
value class OhmUrl(val str:String) {
    companion object{
        const val PREFIX_MODULE = "@bundle:"
        const val PREFIX_THIRD_PARTY = "@package:"
        const val PREFIX_NATIVE_LIB = "@app:"
        const val PREFIX_NATIVE_SYS = "@native:"
        const val PREFIX_OHOS = "@ohos:"
    }
    override fun toString(): String {
        return str
    }
}