package me.yricky.oh.abcd.isa


sealed class InsRegUnit(val size:Int)
data object V4V4:InsRegUnit(1)
data object V8:InsRegUnit(1)
data object V16:InsRegUnit(1)
data object IMM4IMM4:InsRegUnit(1)
data object IMM8:InsRegUnit(1)
data object IMM16:InsRegUnit(2)
data object IMM32:InsRegUnit(4)
data object IMM64:InsRegUnit(4)
data object ID16:InsRegUnit(2)
class InsFmt(val units:List<InsRegUnit>){
    companion object{
        val NONE = InsFmt(emptyList())
        val V42 = InsFmt(listOf(V4V4))
        val IMM42 = InsFmt(listOf(IMM4IMM4))
        fun fromString(str: String):InsFmt{
            return when(str){
                "V4_V4" -> V42
                "IMM4_IMM4" -> IMM42
                else -> {
                    InsFmt(str.split("_").map {
                        when(it){
                            "V8" -> V8
                            "V16" -> V16
                            "IMM8" -> IMM8
                            "IMM16" -> IMM16
                            "IMM32" -> IMM32
                            "IMM64" -> IMM64
                            "ID16" -> ID16
                            else -> throw IllegalStateException("No such unit:${it}")
                        }
                    })
                }
            }
        }
    }
}



