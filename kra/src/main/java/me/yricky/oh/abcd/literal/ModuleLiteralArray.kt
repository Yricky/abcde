package me.yricky.oh.abcd.literal

import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.utils.DataAndNextOff
import me.yricky.oh.utils.nextOffset
import me.yricky.oh.utils.stringItem
import me.yricky.oh.utils.value

class ModuleLiteralArray(
    val abc:AbcBuf,
    val offset:Int
) {
    val literalNum = abc.buf.getInt(offset)
    val moduleRequestNum = abc.buf.getInt(offset + 4)
    private val _moduleRequests by lazy {
        (0 until moduleRequestNum).map {
            stringItem(abc.buf,abc.buf.getInt(offset + 8 + 4 * it)).value
        }.let { DataAndNextOff(it,offset + 8 + (4 * moduleRequestNum)) }
    }
    val moduleRequests get() = _moduleRequests.value

    val regularImportNum = abc.buf.getInt(_moduleRequests.nextOffset)
    private val _regularImports by lazy {
        (0 until regularImportNum).map {
            RegularImport.parseFrom(this, _moduleRequests.nextOffset + 4 + 10 * it)
        }.let { DataAndNextOff(it,_moduleRequests.nextOffset + 4 + (10 * regularImportNum)) }
    }
    val regularImports get() = _regularImports.value

    val namespaceImportNum by lazy { abc.buf.getInt(_regularImports.nextOffset) }
    private val _namespaceImports by lazy {
        (0 until regularImportNum).map {
            NamespaceImport.parseFrom(this,_regularImports.nextOffset + 4 + 6 * it)
        }.let { DataAndNextOff(it,_regularImports.nextOffset + 4 + 6 * namespaceImportNum) }
    }
    val namespaceImports = _namespaceImports.value

    val localExportNum by lazy { abc.buf.getInt(_namespaceImports.nextOffset) }
    val _localExports by lazy {
        (0 until localExportNum).map {
            LocalExport.parseFrom(this,_namespaceImports.nextOffset + 4 + 8 * it)
        }.let { DataAndNextOff(it,_namespaceImports.nextOffset + 4 + 8 * localExportNum) }
    }

    val indirectExportNum by lazy { abc.buf.getInt(_localExports.nextOffset) }
    val _indirectExports by lazy {
        (0 until indirectExportNum).map {
            IndirectExport.parseFrom(this,_localExports.nextOffset + 4 + 10 * it)
        }.let { DataAndNextOff(it,_localExports.nextOffset + 4 + 10 * indirectExportNum) }
    }

    val starExportNum by lazy { abc.buf.getInt(_indirectExports.nextOffset) }
    val _starExports by lazy {
        (0 until starExportNum).map {
            StarExport.parseFrom(this,_indirectExports.nextOffset + 4 + 2 * it)
        }.let { DataAndNextOff(it,_indirectExports.nextOffset + 4 + 2 * starExportNum) }
    }

    data class RegularImport(
        val mla: ModuleLiteralArray,
        val localNameOffset:Int,
        val importNameOffset:Int,
        val moduleRequestIdx:UShort
    ){
        companion object{
            fun parseFrom(mla: ModuleLiteralArray,offset: Int):RegularImport{
                return RegularImport(
                    mla,
                    localNameOffset = mla.abc.buf.getInt(offset),
                    importNameOffset = mla.abc.buf.getInt(offset + 4),
                    moduleRequestIdx = mla.abc.buf.getShort(offset + 8).toUShort()
                )
            }
        }
    }

    data class NamespaceImport(
        val mla:ModuleLiteralArray,
        val localNameOffset:Int,
        val moduleRequestIdx:UShort
    ){
        companion object{
            fun parseFrom(mla: ModuleLiteralArray,offset: Int):NamespaceImport{
                return NamespaceImport(
                    mla,
                    localNameOffset = mla.abc.buf.getInt(offset),
                    moduleRequestIdx = mla.abc.buf.getShort(offset + 4).toUShort()
                )
            }
        }
    }

    data class LocalExport(
        val mla: ModuleLiteralArray,
        val localNameOffset:Int,
        val exportNameOffset:Int
    ){
        companion object{
            fun parseFrom(mla: ModuleLiteralArray,offset: Int):LocalExport{
                return LocalExport(
                    mla,
                    localNameOffset = mla.abc.buf.getInt(offset),
                    exportNameOffset = mla.abc.buf.getInt(offset + 4),
                )
            }
        }
    }

    data class IndirectExport(
        val mla: ModuleLiteralArray,
        val exportNameOffset:Int,
        val importNameOffset:Int,
        val moduleRequestIdx:UShort
    ){
        companion object{
            fun parseFrom(mla: ModuleLiteralArray,offset: Int):IndirectExport{
                return IndirectExport(
                    mla,
                    exportNameOffset = mla.abc.buf.getInt(offset),
                    importNameOffset = mla.abc.buf.getInt(offset + 4),
                    moduleRequestIdx = mla.abc.buf.getShort(offset + 8).toUShort()
                )
            }
        }
    }

    data class StarExport(
        val mla: ModuleLiteralArray,
        val moduleRequestIdx:UShort
    ){
        companion object{
            fun parseFrom(mla: ModuleLiteralArray,offset: Int):StarExport{
                return StarExport(
                    mla,
                    moduleRequestIdx = mla.abc.buf.getShort(offset).toUShort()
                )
            }
        }
    }
}