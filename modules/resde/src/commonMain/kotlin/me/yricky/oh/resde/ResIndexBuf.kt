package me.yricky.oh.resde

import me.yricky.oh.common.BufOffset
import me.yricky.oh.common.LEByteBuf

class ResIndexBuf(
    override val buf: LEByteBuf
) :BufOffset {
    override val offset: Int get() = 0

    val header = ResIndexHeader(buf)
    val limitKeyConfigs:List<LimitKeyConfig> by lazy {
        val list = ArrayList<LimitKeyConfig>(header.limitKeyConfigCount)
        var off = 136
        repeat(header.limitKeyConfigCount){
            off +=4 // "KEYS"
            val kOffset = buf.getInt(off)
            off += 4
            val keyCount = buf.getInt(off)
            off += 4
            val param = ArrayList<LimitKeyConfig.KeyParam>(keyCount)
            repeat(keyCount){
                param.add(LimitKeyConfig.KeyParam(buf.getLong(off)))
                off += 8
            }
            list.add(LimitKeyConfig(kOffset,keyCount,param))
        }
        list
    }
}