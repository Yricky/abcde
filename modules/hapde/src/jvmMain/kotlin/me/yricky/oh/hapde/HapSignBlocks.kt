package me.yricky.oh.hapde

import me.yricky.oh.common.LEByteBuf

class HapSignBlocks(
    val offset:Long,
    val version:Int,
    val content:List<SignBlock>
){
    fun getSignatureSchemeBlock():SignBlock = content.first { it.type == SIGNATURE_SCHEME_V1_BLOCK_ID }
    fun getPropertyBlock():SignBlock? = content.firstOrNull { it.type == PROPERTY_BLOCK_ID }
    fun getProfileBlock():SignBlock? = content.firstOrNull { it.type == PROFILE_BLOCK_ID }

    companion object{
        private const val HEADER_SIZE: Int = 32

        private const val MAGIC_LO_V2: Long = 0x2067695320504148L
        private const val MAGIC_HI_V2: Long = 0x3234206b636f6c42L
        private const val MAGIC_LO_V3: Long = 0x676973207061683cL
        private const val MAGIC_HI_V3: Long = 0x3e6b636f6c62206eL

        const val SIGNATURE_SCHEME_V1_BLOCK_ID: Int = 0x20000000
        const val PROOF_OF_ROTATION_BLOCK_ID: Int = 0x20000001
        const val PROFILE_BLOCK_ID: Int = 0x20000002
        const val PROPERTY_BLOCK_ID: Int = 0x20000003

        fun from(
            hap:LEByteBuf,
            info: HapFileInfo
        ):HapSignBlocks?{
            val hapSigningBlockHeaderOffset = info.centralDirectoryOffset - HEADER_SIZE
            val header = hap.slice(hapSigningBlockHeaderOffset.toInt(), HEADER_SIZE)

            val blockCount = header.getInt(0)
            val sigBlockSize = header.getLong(4)
            val sigBlockMagicLo = header.getLong(12)
            val sigBlockMagicHi = header.getLong(20)
            val version = header.getInt(28)

            if(version < 3 && (sigBlockMagicLo != MAGIC_LO_V2 || sigBlockMagicHi != MAGIC_HI_V2)){
                return null
            } else if(version == 3 && (sigBlockMagicLo != MAGIC_LO_V3 || sigBlockMagicHi != MAGIC_HI_V3)){
                return null
            }

            val sigBlockOffset = info.centralDirectoryOffset - sigBlockSize
            val content = hap.slice(sigBlockOffset.toInt(), sigBlockSize.toInt())

            val blocks = (0 until blockCount).map { idx ->
                val type = content.getInt(idx * 12)
                val len  = content.getInt(idx * 12 + 4)
                val off  = content.getInt(idx * 12 + 8)
                val blockContent = content.slice(off,len)
                SignBlock(type,len,blockContent)
            }

            return HapSignBlocks(sigBlockOffset, version, blocks)
        }
    }

    class SignBlock(
        val type:Int,
        val length:Int,
        val content:LEByteBuf
    )

}