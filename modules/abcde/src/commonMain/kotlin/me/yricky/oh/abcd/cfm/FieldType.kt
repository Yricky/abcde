package me.yricky.oh.abcd.cfm

import me.yricky.oh.abcd.AbcBuf

sealed class FieldType{
    abstract val name:String
    fun getClass():AbcClass? = (this as? ClassType)?.clazz as? AbcClass

    class PrimitiveType(private val code:Int):FieldType(){
        companion object{
            private val NAME = listOf(
                "u1", "i8", "u8", "i16",
                "u16", "i32", "u32", "f32",
                "f64", "i64", "u64", "any"
            )
        }
        override val name: String get() = NAME[code]
    }

    class ClassType(val clazz: ClassItem):FieldType(){
        override val name: String get() = clazz.name
    }

    companion object{
        fun fromOffset(abc: AbcBuf, offset:Int):FieldType{
            return if(offset <= 0xb){
                PrimitiveType(offset)
            } else ClassType(abc.classes[offset]!!)
        }
    }
}

