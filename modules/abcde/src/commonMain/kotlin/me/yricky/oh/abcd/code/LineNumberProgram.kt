package me.yricky.oh.abcd.code

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.AbcBufOffset
import me.yricky.oh.abcd.isa.Inst.Companion.toUnsignedInt
import me.yricky.oh.utils.readSLeb128
import me.yricky.oh.utils.uleb2sleb

class LineNumberProgram(override val abc: AbcBuf, override val offset: Int) :AbcBufOffset {

    fun eval(info:DebugInfo):DebugState{
        val iterator = info.constantPool.iterator()
        var off = offset
        var sourceCodeString:Int? = null
        val addressLineColumns = ArrayList<LnpItem>()
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
                    addressLineColumns.add(StartLocal(address,line,column, nameIdx, typeIdx))
//                    println("START_LOCAL$registerNum:${abc.stringItem(nameIdx).value},${abc.stringItem(typeIdx).value}")
                }
                START_LOCAL_EXTENDED -> {
                    val registerNum = abc.buf.readSLeb128(off)
                    off = registerNum.nextOffset
                    val nameIdx = iterator.next()
                    val typeIdx = iterator.next()
                    val sigIdx = iterator.next()
                    addressLineColumns.add(StartLocalExt(address,line,column, nameIdx, typeIdx, sigIdx))
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
                        addressLineColumns.add(SetFile(address,line,column, strIdx))
                    }
//                    println("SET_FILE")
                }
                SET_SOURCE_CODE -> {
                    val strIdx = iterator.next()
                    if (strIdx != 0){
                        sourceCodeString = strIdx
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
            sourceCodeString, addressLineColumns, off - offset
        )
    }

    companion object{
        const val END_SEQUENCE:Byte = 0x0
        const val ADVANCE_PC:Byte = 0x1
        const val ADVANCE_LINE:Byte = 0x2
        const val START_LOCAL:Byte = 0x3
        const val START_LOCAL_EXTENDED:Byte = 0x4
        const val END_LOCAL:Byte = 0x5
        const val RESTART_LOCAL:Byte = 0x06
        const val SET_PROLOGUE_END:Byte = 0x07
        const val SET_EPILOGUE_BEGIN:Byte = 0x08
        const val SET_FILE:Byte = 0x9
        const val SET_SOURCE_CODE:Byte = 0xa
        const val SET_COLUMN:Byte = 0xb
        const val SPECIAL_OPCODE_BASE:Byte = 0xc
    }

    data class DebugState(
        val sourceCodeStringOff:Int?,
        val addressLineColumns: List<LnpItem>,
        val lnpSize:Int
    )
}
sealed class LnpItem(val address:Int, val line: Int, val column: Int)
class AddressLineColumn(address: Int, line: Int, column: Int) : LnpItem(address, line, column)
class SetFile(address: Int, line: Int, column: Int, val nameIdx:Int) : LnpItem(address, line, column)
class StartLocal(address: Int, line: Int, column: Int,val nameIdx:Int,val typeIdx:Int) : LnpItem(address, line, column)
class StartLocalExt(address: Int, line: Int, column: Int,val nameIdx:Int,val typeIdx:Int, val sigIdx:Int) : LnpItem(address, line, column)