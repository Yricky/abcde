package me.yricky.oh.abcd

import me.yricky.oh.abcd.cfm.*
import me.yricky.oh.abcd.literal.LiteralArray
import me.yricky.oh.abcd.literal.ModuleLiteralArray
import me.yricky.oh.utils.DataAndNextOff
import me.yricky.oh.utils.MUtf8
import me.yricky.oh.utils.readULeb128
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.WeakHashMap

/**
 * ABC文件解析类入口
 */
class AbcBuf(
    _buf:ByteBuffer
) {
    val buf: ByteBuffer = _buf.order(ByteOrder.LITTLE_ENDIAN)
    val header = AbcHeader(buf)
    val classes by lazy {
        (0 until header.numClasses).associate { i ->
            val classIndex = buf.getInt(header.classIdxOff + i * 4)
            Pair(
                classIndex,
                if(classIndex in header.foreignOff until (header.foreignOff + header.foreignSize)){
                    ForeignClass(this,classIndex)
                } else {
                    ClassItem(this,classIndex)
                }
            )
        }
    }

    val regions by lazy {
        (0 until header.numIndexRegions).map {
            Region(this,header.indexSectionOff + it * 40)
        }
    }

    val literalArrays by lazy {
        (0 until header.numLiteralArrays).map {
            LiteralArray(this,buf.getInt(header.literalArrayIdxOff + it * 4))
        }
    }

    val moduleLiteralArrays by lazy {
        val map = LinkedHashMap<Int,ModuleLiteralArray>()
        classes.forEach { (_, c) ->
            if(c is ClassItem){
                c.fields.firstOrNull { it.isModuleRecordIdx() }?.getIntValue()
                    ?.takeIf { isValidOffset(it) }
                    ?.let { map[it] = ModuleLiteralArray(this,it) }
            }
        }
        map
    }

    fun isValidOffset(offset:Int): Boolean{
        return offset >= 60 && offset < buf.limit()
    }

    //TODO 线程安全
    private val _stringCache = HashMap<Int,DataAndNextOff<String>>()
    fun stringItem(offset:Int):DataAndNextOff<String>{
        return _stringCache[offset].also {
//            println("hitCache:${offset.toString(16)}")
        } ?: run {
            val (utf16Size,strDataOff) = buf.readULeb128(offset)
            MUtf8.getMUtf8String(buf,strDataOff,utf16Size.ushr(1)).also {
                _stringCache[offset] = it
            }
        }
    }
}