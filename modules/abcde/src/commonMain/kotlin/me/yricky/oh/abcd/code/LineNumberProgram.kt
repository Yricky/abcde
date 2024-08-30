package me.yricky.oh.abcd.code

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.AbcBufOffset
import me.yricky.oh.abcd.isa.Inst.Companion.toUnsignedInt
import me.yricky.oh.common.nextOffset
import me.yricky.oh.common.value
import me.yricky.oh.utils.readSLeb128
import me.yricky.oh.utils.uleb2sleb

class LineNumberProgram(override val abc: AbcBuf, override val offset: Int) :AbcBufOffset {

    fun eval(info:DebugInfo):DebugState{
        val iterator = info.constantPool.iterator()
        var off = offset
        var fileString:String? = null
        var sourceCodeString:String? = null
        val addressLineColumns = ArrayList<AddressLineColumn>()
        var end = false
        var address = 0
        var line = info.lineStart
        var column = 0
        do {
            val currOpCode = abc.buf.get(off)
            off++
            when(currOpCode){
                END_SEQUENCE -> {
//                    println("END_SEQUENCE")
                    end = true
                }
                ADVANCE_PC -> {
                    address += iterator.next()
//                    println("ADVANCE_PC$address")
                }
                ADVANCE_LINE -> {
                    line += iterator.next().uleb2sleb()
//                    println("ADVANCE_LINE${line}")

                }
                START_LOCAL -> {
                    val registerNum = abc.buf.readSLeb128(off)
                    off = registerNum.nextOffset
                    val nameIdx = iterator.next()
                    val typeIdx = iterator.next()
//                    println("START_LOCAL$registerNum:${abc.stringItem(nameIdx).value},${abc.stringItem(typeIdx).value}")
                }
                START_LOCAL_EXTENDED -> {
                    val registerNum = abc.buf.readSLeb128(off)
                    off = registerNum.nextOffset
                    val nameIdx = iterator.next()
                    val typeIdx = iterator.next()
                    val sigIdx = iterator.next()
//                    println("START_LOCAL_E$registerNum:${abc.stringItem(nameIdx).value},${abc.stringItem(typeIdx).value},${abc.stringItem(sigIdx).value}")
                }
                END_LOCAL -> {
                    val registerNum = abc.buf.readSLeb128(off)
                    off = registerNum.nextOffset
//                    println("END_LOCAL$registerNum")
                }
                RESTART_LOCAL -> {
                    val registerNum = abc.buf.readSLeb128(off)
                    off = registerNum.nextOffset
//                    println("RESTART_LOCAL$registerNum")
                }
                SET_FILE -> {
                    val strIdx = iterator.next()
                    if (strIdx != 0){
                        fileString = abc.stringItem(strIdx).value
                    }
//                    println("SET_FILE")
                }
                SET_SOURCE_CODE -> {
                    val strIdx = iterator.next()
                    if (strIdx != 0){
                        sourceCodeString = abc.stringItem(strIdx).value
                    }
//                    println("SET_SOURCE_CODE")
                }
                SET_COLUMN -> {
                    column = iterator.next()
                    addressLineColumns.add(AddressLineColumn(address,line,column))
//                    println("SET_COLUMN")
                }
                else -> {
//                    println("$currOpCode")
                    if(currOpCode >= SPECIAL_OPCODE_BASE){
                        val adjOp = currOpCode.toUnsignedInt() - SPECIAL_OPCODE_BASE
                        address += adjOp / 15
                        line += adjOp % 15 - 4
                        addressLineColumns.add(AddressLineColumn(address,line,column))
                    }
                }
            }
        }while (!end)
        return DebugState(
            fileString, sourceCodeString, addressLineColumns
        )
    }

    companion object{
        val END_SEQUENCE:Byte = 0x0
        val ADVANCE_PC:Byte = 0x1
        val ADVANCE_LINE:Byte = 0x2
        val START_LOCAL:Byte = 0x3
        val START_LOCAL_EXTENDED:Byte = 0x4
        val END_LOCAL:Byte = 0x5
        val RESTART_LOCAL:Byte = 0x06
        val SET_PROLOGUE_END:Byte = 0x07
        val SET_EPILOGUE_BEGIN:Byte = 0x08
        val SET_FILE:Byte = 0x9
        val SET_SOURCE_CODE:Byte = 0xa
        val SET_COLUMN:Byte = 0xb
        val SPECIAL_OPCODE_BASE:Byte = 0xc
    }

    data class DebugState(
        val fileString:String?,
        val sourceCodeString:String?,
        val addressLineColumns: List<AddressLineColumn>
    )
}

typealias AddressLineColumn = Triple<Int,Int,Int>
val AddressLineColumn.address get() = first
val AddressLineColumn.line get() = second
val AddressLineColumn.column get() = third