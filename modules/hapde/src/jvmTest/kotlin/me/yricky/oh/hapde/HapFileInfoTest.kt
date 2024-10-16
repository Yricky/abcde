package me.yricky.oh.hapde

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import me.yricky.oh.common.toByteArray
import me.yricky.oh.common.wrapAsLEByteBuf
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cms.CMSSignedData
import org.junit.Test
import java.io.File
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class HapFileInfoTest{
    val file = File("/Users/Yricky/Downloads/ohbili-v0.1.1.hap")
    val mmap = FileChannel.open(file.toPath()).map(FileChannel.MapMode.READ_ONLY,0,file.length())
    val hap = wrapAsLEByteBuf(mmap.order(ByteOrder.LITTLE_ENDIAN))
    @Test
    fun test(){
        val info = HapFileInfo.from(hap)?.also {
            println("cdSize:${it.centralDirectorySize}")
            println("cdOff:${it.centralDirectoryOffset}")
            println("cdEC:${it.centralDirectoryEntryCount}")
        }
        if(info != null){
            val a = HapSignBlocks.from(hap)!!
            with(Json { prettyPrint = true }){
                println(encodeToString(JsonElement.serializer(),decodeFromString(a.getProfileContent() ?: "")).replace("\\n","\n"))
            }

            val converter = JcaX509CertificateConverter()
            val cms = CMSSignedData(a.getSignatureSchemeBlock().content.toByteArray())

            cms.certificates.getMatches(null).forEach {
                val cert = converter.getCertificate(it)
                println("cert:${cert}")
            }
        }
    }
}