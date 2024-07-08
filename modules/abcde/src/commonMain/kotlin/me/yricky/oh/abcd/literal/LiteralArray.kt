package me.yricky.oh.abcd.literal

import me.yricky.oh.abcd.AbcBufOffset
import me.yricky.oh.common.LEByteBuf
import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.cfm.MethodItem
import me.yricky.oh.common.DataAndNextOff
import me.yricky.oh.common.nextOffset
import me.yricky.oh.common.value

class LiteralArray(
    override val abc: AbcBuf,
    override val offset: Int
): AbcBufOffset {
    private val _size by lazy {
        abc.buf.getInt(offset)
    }
    val content: List<Literal> by lazy {
        val list = ArrayList<Literal>()
        var i = 0
        var off = offset + 4
        while (i < _size) { //这是oh官方源码里的写法……逆天
//        repeat(size) {
            val literal = Literal.parseLiteral(abc.buf,off)
            list.add(literal.value)
            off = literal.nextOffset
            if(list.lastOrNull() is Literal.ArrRef){
                i = _size
            } else {
                i += 2
            }
//            println("list.size:${list.size}")
        }
        list
    }
    val size get() = content.size

    override fun toString():String{
        val sb = StringBuilder()
        sb.append("{ $size [ ")
        content.forEach {
            if(it is Literal.Str){
                sb.append("str:\"${it.get(abc)}\", ")
            } else if(it is Literal.LiteralMethod) {
                val method = abc.method(it.offset)
                sb.append("${it::class.simpleName}:${method.clazz.name}.${method.name}, ")
            }else {
                sb.append("${it}, ")
            }
        }
        sb.append(" ]}")
        return sb.toString()
    }
//    val size
//        get() = if (_size % 2 == 0) {
//            _size.ushr(1)
//        } else _size.ushr(2)
//    @Deprecated("猜的")
//    val flag get() = _size % 2 != 0

    sealed class Literal {
        companion object {
            fun parseLiteral(buf: LEByteBuf, offset: Int): DataAndNextOff<Literal> {
                var off = offset
                return when (val tag = buf.get(off++)) {
                    TagValue.TAG -> { DataAndNextOff(TagValue(buf.get(off)), off + 1) }
                    Bool.TAG -> { DataAndNextOff(Bool(buf.get(off)), off + 1) }
                    I32.TAG -> { DataAndNextOff((I32(buf.getInt(off))), off + 4) }
                    F32.TAG -> { DataAndNextOff((F32(Float.fromBits(buf.getInt(off)))), off + 4) }
                    F64.TAG -> { DataAndNextOff((F64(Double.fromBits(buf.getLong(off)))), off + 8) }
                    Str.TAG -> { DataAndNextOff((Str(buf.getInt(off))), off + 4) }
                    LiteralBufferIndex.TAG -> { DataAndNextOff((LiteralBufferIndex(buf.getInt(off))), off + 4) }
                    Method.TAG -> { DataAndNextOff((Method(buf.getInt(off))), off + 4) }
                    Getter.TAG -> { DataAndNextOff((Getter(buf.getInt(off))), off + 4) }
                    Setter.TAG -> { DataAndNextOff((Setter(buf.getInt(off))), off + 4) }
                    GeneratorMethod.TAG -> { DataAndNextOff((GeneratorMethod(buf.getInt(off))), off + 4) }
                    AsyncGeneratorMethod.TAG -> { DataAndNextOff((AsyncGeneratorMethod(buf.getInt(off))), off + 4) }
                    Accessor.TAG -> { DataAndNextOff((Accessor(buf.get(off))), off + 1) }
                    BuiltinTypeIndex.TAG -> { DataAndNextOff((BuiltinTypeIndex(buf.get(off))), off + 1) }
                    NullValue.TAG -> { DataAndNextOff((NullValue(buf.get(off))), off + 1) }
                    MethodAffiliate.TAG -> { DataAndNextOff((MethodAffiliate(buf.getShort(off))), off + 2) }
                    LiteralArr.TAG -> { DataAndNextOff((LiteralArr(buf.getInt(off))), off + 4) }
                    ArrayU1.TAG -> { DataAndNextOff((ArrayU1(buf.getInt(off))), off + 4) }
                    ArrayU8.TAG -> { DataAndNextOff((ArrayU8(buf.getInt(off))), off + 4) }
                    ArrayI8.TAG -> { DataAndNextOff((ArrayI8(buf.getInt(off))), off + 4) }
                    ArrayU16.TAG -> { DataAndNextOff((ArrayU16(buf.getInt(off))), off + 4) }
                    ArrayI16.TAG -> { DataAndNextOff((ArrayI16(buf.getInt(off))), off + 4) }
                    ArrayU32.TAG -> { DataAndNextOff((ArrayU32(buf.getInt(off))), off + 4) }
                    ArrayI32.TAG -> { DataAndNextOff((ArrayI32(buf.getInt(off))), off + 4) }
                    ArrayU64.TAG -> { DataAndNextOff((ArrayU64(buf.getInt(off))), off + 4) }
                    ArrayI64.TAG -> { DataAndNextOff((ArrayI64(buf.getInt(off))), off + 4) }
                    ArrayF32.TAG -> { DataAndNextOff((ArrayF32(buf.getInt(off))), off + 4) }
                    ArrayF64.TAG -> { DataAndNextOff((ArrayF64(buf.getInt(off))), off + 4) }
                    ArrayStr.TAG -> { DataAndNextOff((ArrayStr(buf.getInt(off))), off + 4) }
                    else -> throw IllegalStateException("No such tag:${tag.toString(16)},off:${off.toString(16)}")
                }

            }
        }

        sealed class LiteralDirect<T>(val value: T) : Literal(){
            override fun toString(): String {
                return "${this::class.simpleName}:$value"
            }
        }
        sealed class LiteralRef(val offset: Int) : Literal(){
            override fun toString(): String {
                return "${this::class.simpleName}:${offset.toString(16)}"
            }
        }
        sealed class LiteralMethod(offset: Int) :LiteralRef(offset){
            fun get(abc: AbcBuf) : MethodItem = abc.method(offset)
        }
        sealed class ArrRef(offset: Int) : LiteralRef(offset){
            override fun toString(): String {
                return "${this::class.simpleName}:${offset.toString(16)}"
            }
        }

        //class TAGVALUE {companion object{const val TAG = 0x00.toByte()}}
        class Bool(value: Byte) : LiteralDirect<Byte>(value) {
            companion object {
                const val TAG = 0x01.toByte()
            }
        }

        class I32(value: Int) : LiteralDirect<Int>(value) {
            companion object {
                const val TAG = 0x02.toByte()
            }
        }

        class F32(value: Float) : LiteralDirect<Float>(value) {
            companion object {
                const val TAG = 0x03.toByte()
            }
        }

        class F64(value: Double) : LiteralDirect<Double>(value) {
            companion object {
                const val TAG = 0x04.toByte()
            }
        }

        class Str(offset: Int) : LiteralRef(offset) {
            companion object {
                const val TAG = 0x05.toByte()
            }
            fun get(abc: AbcBuf) = abc.stringItem(offset).value
        }

        class Method(offset: Int) : LiteralMethod(offset) {
            companion object {
                const val TAG = 0x06.toByte()
            }
        }

        class GeneratorMethod(offset: Int) : LiteralMethod(offset) {
            companion object {
                const val TAG = 0x07.toByte()
            }
        }

        class Accessor(value: Byte) : LiteralDirect<Byte>(value) {
            companion object {
                const val TAG = 0x08.toByte()
            }
        }

        class MethodAffiliate(value: Short) : LiteralDirect<Short>(value) {
            companion object {
                const val TAG = 0x09.toByte()
            }
        }

        class ArrayU1(offset: Int) : ArrRef(offset) {
            companion object {
                const val TAG = 0x0a.toByte()
            }
        }

        class ArrayU8(offset: Int) : ArrRef(offset) {
            companion object {
                const val TAG = 0x0b.toByte()
            }
        }

        class ArrayI8(offset: Int) : ArrRef(offset) {
            companion object {
                const val TAG = 0x0c.toByte()
            }
        }

        class ArrayU16(offset: Int) : ArrRef(offset) {
            companion object {
                const val TAG = 0x0d.toByte()
            }
        }

        class ArrayI16(offset: Int) : ArrRef(offset) {
            companion object {
                const val TAG = 0x0e.toByte()
            }
        }

        class ArrayU32(offset: Int) : ArrRef(offset) {
            companion object {
                const val TAG = 0x0f.toByte()
            }
        }

        class ArrayI32(offset: Int) : ArrRef(offset) {
            companion object {
                const val TAG = 0x10.toByte()
            }
        }

        class ArrayU64(offset: Int) : ArrRef(offset) {
            companion object {
                const val TAG = 0x11.toByte()
            }
        }

        class ArrayI64(offset: Int) : ArrRef(offset) {
            companion object {
                const val TAG = 0x12.toByte()
            }
        }

        class ArrayF32(offset: Int) : ArrRef(offset) {
            companion object {
                const val TAG = 0x13.toByte()
            }
        }

        class ArrayF64(offset: Int) : ArrRef(offset) {
            companion object {
                const val TAG = 0x14.toByte()
            }
        }

        class ArrayStr(offset: Int) : ArrRef(offset) {
            fun get(abc: AbcBuf):List<String>{
                val len = abc.buf.getInt(offset)
                return (1 .. len).map {
                    val strOffset = abc.buf.getInt(offset + it * 4)
                    abc.stringItem(strOffset).value
                }
            }
            companion object {
                const val TAG = 0x15.toByte()
            }
        }

        class AsyncGeneratorMethod(offset: Int) : LiteralMethod(offset) {
            companion object {
                const val TAG = 0x16.toByte()
            }
        }

        class LiteralBufferIndex(value: Int) : LiteralDirect<Int>(value) {
            companion object {
                const val TAG = 0x17.toByte()
            }
        }

        class LiteralArr(offset: Int) : LiteralRef(offset) {
            companion object {
                const val TAG = 0x18.toByte()
            }
        }

        class BuiltinTypeIndex(value: Byte) : LiteralDirect<Byte>(value) {
            companion object {
                const val TAG = 0x19.toByte()
            }
        }

        class Getter(offset: Int) : LiteralMethod(offset) {
            companion object {
                const val TAG = 0x1a.toByte()
            }
        }

        class Setter(offset: Int) : LiteralMethod(offset) {
            companion object {
                const val TAG = 0x1b.toByte()
            }
        }

        class NullValue(value: Byte) : LiteralDirect<Byte>(value) {
            companion object {
                const val TAG = 0xff.toByte()
            }
        }
        class TagValue(value: Byte) : LiteralDirect<Byte>(value) {
            companion object {
                const val TAG = 0x00.toByte()
            }
        }
    }
}