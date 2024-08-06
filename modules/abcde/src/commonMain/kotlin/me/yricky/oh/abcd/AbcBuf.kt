package me.yricky.oh.abcd

import me.yricky.oh.common.BufOffset
import me.yricky.oh.common.LEByteBuf
import me.yricky.oh.abcd.cfm.*
import me.yricky.oh.abcd.code.LineNumberProgram
import me.yricky.oh.abcd.literal.LiteralArray
import me.yricky.oh.common.DataAndNextOff
import me.yricky.oh.utils.*
import java.util.concurrent.ConcurrentHashMap

/**
 * ABC文件解析类入口
 *
 * @param tag 这个abc文件的tag，用于区分不同的abc文件，通常是文件名或者路径
 */
class AbcBuf(
    val tag:String,
    override val buf: LEByteBuf
): BufOffset {
    val header = AbcHeader(buf)
    val classes by lazy {
        (0 until header.numClasses).associate { i ->
            val classIndex = buf.getInt(header.classIdxOff + i * 4)
            Pair(
                classIndex,
                if(isForeignOffset(classIndex)){
                    ForeignClass(this,classIndex)
                } else {
                    AbcClass(this,classIndex)
                }
            )
        }
    }

    val regions by lazy {
        (0 until header.numIndexRegions).map {
            Region(this, header.indexSectionOff + it * 40)
        }
    }

    val lnps by lazy {
        (0 until header.numLnps).map {
            LineNumberProgram(this,buf.getInt(header.lnpIdxOff + it * 4))
        }
    }

//    @Uncleared("reserved")
//    val literalArrays by lazy {
//        (0 until header.numLiteralArrays).map {
//            LiteralArray(this,buf.getInt(header.literalArrayIdxOff + it * 4))
//        }
//    }

    fun isValidOffset(offset:Int): Boolean{
        return offset >= 60 && offset < buf.limit()
    }

    fun isForeignOffset(offset:Int): Boolean{
        return offset in header.foreignOff until (header.foreignOff + header.foreignSize)
    }

    //TODO 线程安全
    private val _stringCache = ConcurrentHashMap<Int,DataAndNextOff<String>>()
    fun stringItem(offset:Int):DataAndNextOff<String>{
        return _stringCache[offset] ?: run {
            val (utf16Size,strDataOff) = buf.readULeb128(offset)
            MUtf8.getMUtf8String(buf,strDataOff,utf16Size.ushr(1)).also {
                _stringCache[offset] = it
            }
        }
    }

    private val _methodCache = ConcurrentHashMap<Int,MethodItem>()
    fun method(offset: Int):MethodItem{
        return _methodCache[offset] ?: (if(isForeignOffset(offset)) ForeignMethod(this,offset) else AbcMethod(this,offset)).also {
            _methodCache[offset] = it
        }
    }

    private val _laCache = ConcurrentHashMap<Int,LiteralArray>()
    fun literalArray(offset: Int):LiteralArray{
        return _laCache[offset] ?: LiteralArray(this,offset).also {
            _laCache[offset] = it
        }
    }

    override val offset: Int get() = 0
}