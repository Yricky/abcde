package me.yricky.oh.abcd.cfm

import me.yricky.oh.abcd.AbcBufOffset
import me.yricky.oh.abcd.AbcBuf

class ParamAnnotation(
    override val abc: AbcBuf,
    override val offset:Int
): AbcBufOffset {
    val count = abc.buf.getInt(offset)
    val array by lazy {
        val list = ArrayList<AnnotationArray>(count)
        var off = offset + 4
        repeat(count){
            val arr = AnnotationArray(abc, off)
            off += (4 + 4 * arr.count)
            list.add(arr)
        }
        list
    }

    class AnnotationArray(
        val abc: AbcBuf,
        val offset:Int
    ){
        val count = abc.buf.getInt(offset)
        val annotations by lazy {
            (0 until count).map {
                AbcAnnotation(abc,offset + 4 + 4 * it)
            }
        }
    }
}