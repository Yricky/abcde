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
        const val PREFIX_HMS = "@hms:"
        const val PREFIX_NORMALIZED = "@normalized:"
        const val PREFIX_NORMALIZED_SO = "@normalized:Y"
        const val PREFIX_NORMALIZED_NOT_CROSS_HAP_FILE = "@normalized:N&&&"
    }
    override fun toString(): String {
        return str
    }

    /**
     * 这些方法暂时不保证准确
     */
    fun isAbcModule():Boolean = str.startsWith(PREFIX_MODULE) ||
            str.startsWith(PREFIX_THIRD_PARTY) ||
            str.startsWith(PREFIX_NORMALIZED_NOT_CROSS_HAP_FILE)
    fun isSystemModule():Boolean = str.startsWith(PREFIX_OHOS)
    fun isNativeModule():Boolean = str.startsWith(PREFIX_NATIVE_LIB) ||
            str.startsWith(PREFIX_NORMALIZED_SO)
    fun isNormalizedUrl():Boolean = str.startsWith(PREFIX_NORMALIZED)
}