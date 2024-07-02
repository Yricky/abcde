package me.yricky.oh.abcd.cfm

import me.yricky.oh.abcd.AbcBufOffset
import me.yricky.oh.abcd.AbcBuf

@Deprecated("since 12.0.1.0")
class Proto(
    override val abc: AbcBuf,
    override val offset:Int
): AbcBufOffset {
    companion object{
        val TYPE_MAP = listOf(
            "void", "u1", "i8", "u8",
            "i16", "u16", "i32", "u32",
            "f32", "f64", "i64", "u64",
            "ref", "any"
        )
        const val TYPE_MASK = 0b1111
    }
    val shorty:List<String> by lazy {
        var off = offset
        val li = mutableListOf<String>()
        var curr :Int
        do {
            curr = abc.buf.getShort(off).toUShort().toInt()
            var thisCurr = curr
            off += 2
            while (thisCurr != 0){
                li.add(TYPE_MAP[(thisCurr and TYPE_MASK) - 1])
                thisCurr = thisCurr.ushr(4)
            }
        } while (curr.ushr(12) != 0)
        li
    }
    val shortyReturn:String get() = shorty.first()
    val shortyParams:List<String> get() = shorty.subList(1,shorty.size)
}