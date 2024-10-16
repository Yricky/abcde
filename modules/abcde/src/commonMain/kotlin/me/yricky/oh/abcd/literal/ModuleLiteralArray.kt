package me.yricky.oh.abcd.literal

import me.yricky.oh.abcd.AbcBufOffset
import me.yricky.oh.abcd.AbcBuf
import me.yricky.oh.abcd.AbcHeader
import me.yricky.oh.common.DataAndNextOff
import me.yricky.oh.common.nextOffset
import me.yricky.oh.common.value

class ModuleLiteralArray(
    override val abc: AbcBuf,
    override val offset:Int
): AbcBufOffset {
    val literalNum = abc.buf.getInt(offset)
    val moduleRequestNum = abc.buf.getInt(offset + 4)
    private val _moduleRequests by lazy {
        (0 until moduleRequestNum).map {
            abc.stringItem(abc.buf.getInt(offset + 8 + 4 * it)).value.let { OhmUrl(it) }
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
        (0 until namespaceImportNum).map {
            NamespaceImport.parseFrom(this,_regularImports.nextOffset + 4 + 6 * it)
        }.let { DataAndNextOff(it,_regularImports.nextOffset + 4 + 6 * namespaceImportNum) }
    }
    val namespaceImports get() = _namespaceImports.value

    val localExportNum by lazy { abc.buf.getInt(_namespaceImports.nextOffset) }
    private val _localExports by lazy {
        (0 until localExportNum).map {
            LocalExport.parseFrom(this,_namespaceImports.nextOffset + 4 + 8 * it)
        }.let { DataAndNextOff(it,_namespaceImports.nextOffset + 4 + 8 * localExportNum) }
    }
    val localExports get() = _localExports.value

    val indirectExportNum by lazy { abc.buf.getInt(_localExports.nextOffset) }
    private val _indirectExports by lazy {
        (0 until indirectExportNum).map {
            IndirectExport.parseFrom(this,_localExports.nextOffset + 4 + 10 * it)
        }.let { DataAndNextOff(it,_localExports.nextOffset + 4 + 10 * indirectExportNum) }
    }
    val indirectExports get() = _indirectExports.value

    val starExportNum by lazy { abc.buf.getInt(_indirectExports.nextOffset) }
    private val _starExports by lazy {
        (0 until starExportNum).map {
            StarExport.parseFrom(this,_indirectExports.nextOffset + 4 + 2 * it)
        }.let { DataAndNextOff(it,_indirectExports.nextOffset + 4 + 2 * starExportNum) }
    }
    val starExports get() = _starExports.value

    class RegularImport(
        val mla: ModuleLiteralArray,
        val localNameOffset:Int,
        val importNameOffset:Int,
        val moduleRequestIdx:UShort
    ){
        val localName :String? = if(localNameOffset in (AbcHeader.SIZE until mla.abc.buf.limit())){
            mla.abc.stringItem(localNameOffset).value
        } else null

        val importName :String? = if(importNameOffset in (AbcHeader.SIZE until mla.abc.buf.limit())){
            mla.abc.stringItem(importNameOffset).value
        } else null

        val moduleRequest: OhmUrl? = if(moduleRequestIdx.toInt() in (0 until mla.moduleRequestNum)){
            mla.moduleRequests[moduleRequestIdx.toInt()]
        } else null

        override fun toString(): String {
            return "import { ${ if(importName == localName) importName else "$importName as $localName" } } from \"$moduleRequest\""
        }
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
        val localName :String? = if(localNameOffset in (AbcHeader.SIZE until mla.abc.buf.limit())){
            mla.abc.stringItem(localNameOffset).value
        } else null

        val moduleRequest: OhmUrl? = if(moduleRequestIdx.toInt() in (0 until mla.moduleRequestNum)){
            mla.moduleRequests[moduleRequestIdx.toInt()]
        } else null
        override fun toString(): String {
            return "import * as $localName from \"$moduleRequest\""
        }
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
        val localName :String? = if(localNameOffset in (AbcHeader.SIZE until mla.abc.buf.limit())){
            mla.abc.stringItem(localNameOffset).value
        } else null

        val exportName :String? = if(exportNameOffset in (AbcHeader.SIZE until mla.abc.buf.limit())){
            mla.abc.stringItem(exportNameOffset).value
        } else null
        override fun toString(): String {
            return "export { ${if(localName == exportName) localName else "$localName as $exportName"} }"
        }
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
        val exportName :String? = if(exportNameOffset in (AbcHeader.SIZE until mla.abc.buf.limit())){
            mla.abc.stringItem(exportNameOffset).value
        } else null

        val importName :String? = if(importNameOffset in (AbcHeader.SIZE until mla.abc.buf.limit())){
            mla.abc.stringItem(importNameOffset).value
        } else null

        val moduleRequest: OhmUrl? = if(moduleRequestIdx.toInt() in (0 until mla.moduleRequestNum)){
            mla.moduleRequests[moduleRequestIdx.toInt()]
        } else null
        override fun toString(): String {
            return "export { ${if(exportName == importName) exportName else "$importName as $exportName" } from \"$moduleRequest\""
        }
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
        val moduleRequest: OhmUrl? = if(moduleRequestIdx.toInt() in (0 until mla.moduleRequestNum)){
            mla.moduleRequests[moduleRequestIdx.toInt()]
        } else null
        override fun toString(): String {
            return "export * from \"$moduleRequest\""
        }
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