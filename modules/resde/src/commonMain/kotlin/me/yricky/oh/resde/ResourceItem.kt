package me.yricky.oh.resde

import me.yricky.oh.common.wrapAsLEByteBuf
import java.nio.ByteBuffer

class ResourceItem(
    val fileName:String,
    val keyParams:List<LimitKeyConfig.KeyParam>,
    val resType:ResType,
    data: ByteArray
){
    val data = Data.from(resType, data)
    val limitKey = run {
        if(keyParams.isEmpty()){
            "base"
        } else {
            keyParams.fold("") { s, p ->
                if (p.keyType == LimitKeyConfig.KeyParam.TYPE_MNC || p.keyType == LimitKeyConfig.KeyParam.TYPE_REGION) {
                    "${s}_$p"
                } else {
                    "${s}-${p}"
                }
            }.removePrefix("-")
        }
    }

    sealed class Data(val raw: ByteArray) {
        protected var string :String? = null
        val asString: String get() = string ?: toString().also { string = it }

        sealed class DirectString(raw: ByteArray) : Data(raw){
            override fun toString(): String {
                return String(raw)
            }
        }
        class PlainString(raw: ByteArray): DirectString(raw)
        class ColorString(raw: ByteArray): DirectString(raw)
        class StringArray(raw: ByteArray): Data(raw) {
            override fun toString(): String {
                val bb = wrapAsLEByteBuf(ByteBuffer.wrap(raw))
                val list = mutableListOf<String>()
                var off = 0
                while (off < raw.size){
                    val len = bb.getShort(off)
                    off += 2
                    val ba = ByteArray(len.toInt())
                    bb.get(off,ba)
                    list.add(String(ba))
                    off += (len + 1) //鸿蒙在前缀size和末尾0之间选择了同时使用，是不是非常大胆？
                }
                return list.toString()
            }
        }

        companion object{
            fun from(type: ResType, raw: ByteArray): Data{
                return when(type){
                    ResType.STRARRAY -> StringArray(raw)
                    ResType.COLOR -> ColorString(raw)
                    else -> PlainString(raw)
                }
            }
        }
    }

}