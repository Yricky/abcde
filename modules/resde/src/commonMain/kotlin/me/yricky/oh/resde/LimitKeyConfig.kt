package me.yricky.oh.resde

import kotlin.jvm.JvmInline

class LimitKeyConfig(
    val idSetOffset:Int,
    val data:List<KeyParam>
) {

    @JvmInline
    value class KeyParam(private val raw:Long){
        val keyType get() = (raw and 0xffffffffL).toInt()
        val value get() = raw.ushr(32).toInt()
        override fun toString(): String {
            return when(keyType){
                TYPE_ORIENTATION -> if(value == 0) "vertical" else "horizontal"
                TYPE_NIGHTMODE -> if(value == 0) "dark" else "light"
                TYPE_DEVICETYPE -> when(value){
                    0 -> "phone"
                    1 -> "tablet"
                    2 -> "car"
                    4 -> "tv"
                    6 -> "wearable"
                    7 -> "2in1"
                    else -> "device${value}"
                }
                TYPE_RESOLUTION -> when(value){
                    120 -> "sdpi"
                    160 -> "mdpi"
                    240 -> "ldpi"
                    320 -> "xldpi"
                    480 -> "xxldpi"
                    640 -> "xxxldpi"
                    else -> "dpi${value}"
                }
                TYPE_LANGUAGE, TYPE_REGION -> {
                    var v = value
                    val sb = StringBuilder()
                    while (v != 0){
                        val code = v and 0xff
                        if(code != 0){
                            sb.append(Char(code))
                        }
                        v = v.ushr(8)
                    }
                    sb.reversed().toString()
                }
                TYPE_MCC -> "mcc$value"
                TYPE_MNC -> "mnc$value"
                else -> "t${keyType}v$value"

            }
        }

        companion object{
            const val TYPE_LANGUAGE = 0
            const val TYPE_REGION = 1
            const val TYPE_RESOLUTION = 2
            const val TYPE_ORIENTATION = 3
            const val TYPE_DEVICETYPE = 4
            const val TYPE_SCRIPT = 5
            const val TYPE_NIGHTMODE = 6
            const val TYPE_MCC = 7
            const val TYPE_MNC = 8
            const val TYPE_RESERVER = 9
            const val TYPE_INPUTDEVICE = 10
        }
    }

    companion object{
        const val TAG = 0x5359454B // "KEYS"
    }
}

/**
 * Map<[LimitKeyConfig.idSetOffset],[LimitKeyConfig.data]>
 */
typealias LimitKeyConfigs = Map<Int,List<LimitKeyConfig.KeyParam>>