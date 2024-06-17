package me.yricky.oh.abcd

import me.yricky.oh.abcd.cfm.*
import me.yricky.oh.abcd.literal.LiteralArray
import me.yricky.oh.abcd.literal.ModuleLiteralArray
import me.yricky.oh.utils.DataAndNextOff
import me.yricky.oh.utils.MUtf8
import me.yricky.oh.utils.Uncleared
import me.yricky.oh.utils.readULeb128
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * ABC文件解析类入口
 *
 * @param tag 这个abc文件的tag，用于区分不同的abc文件，通常是文件名或者路径
 */
class AbcBuf(
    val tag:String,
    _buf:ByteBuffer
) {
    val buf: ByteBuffer = _buf.order(ByteOrder.LITTLE_ENDIAN)
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
            Region(this,header.indexSectionOff + it * 40)
        }
    }

    @Uncleared("reserved")
    val literalArrays by lazy {
        (0 until header.numLiteralArrays).map {
            LiteralArray(this,buf.getInt(header.literalArrayIdxOff + it * 4))
        }
    }

    val moduleLiteralArrays by lazy {
        val map = LinkedHashMap<Int,ModuleLiteralArray>()
        classes.forEach { (_, c) ->
            if(c is AbcClass){
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

    fun isForeignOffset(offset:Int): Boolean{
        return offset in header.foreignOff until (header.foreignOff + header.foreignSize)
    }

    //TODO 线程安全
    private val _stringCache = HashMap<Int,DataAndNextOff<String>>()
    fun stringItem(offset:Int):DataAndNextOff<String>{
        return _stringCache[offset] ?: run {
            val (utf16Size,strDataOff) = buf.readULeb128(offset)
            MUtf8.getMUtf8String(buf,strDataOff,utf16Size.ushr(1)).also {
                _stringCache[offset] = it
            }
        }
    }

    private val _methodCache = HashMap<Int,MethodItem>()
    fun method(offset: Int):MethodItem{
        return _methodCache[offset] ?: (if(isForeignOffset(offset)) ForeignMethod(this,offset) else AbcMethod(this,offset)).also {
            _methodCache[offset] = it
        }
    }
}