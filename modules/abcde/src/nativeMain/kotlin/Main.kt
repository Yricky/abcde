import kotlinx.serialization.json.Json
import me.yricky.oh.abcd.isa.Asm
import me.yricky.oh.abcd.isa.bean.Isa
import me.yricky.oh.abcd.isa.loadInnerAsmMap

fun main(){
    loadInnerAsmMap()
    println(Json.encodeToString(Isa.serializer(),Asm.asmMap.isa))
}