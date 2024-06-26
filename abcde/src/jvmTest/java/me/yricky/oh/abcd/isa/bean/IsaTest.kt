package me.yricky.oh.abcd.isa.bean

//import com.fasterxml.jackson.core.type.TypeReference
//import com.fasterxml.jackson.databind.SerializationFeature
//import com.fasterxml.jackson.databind.json.JsonMapper
//import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
//import me.yricky.oh.abcd.isa.Inst
//import org.junit.Test
//
//
//class IsaTest{
//    @Test
//    fun testYaml(){
//        val yaml = YAMLMapper()
//        val json = JsonMapper().configure(SerializationFeature.INDENT_OUTPUT,true)
//        val isa = yaml.readValue(javaClass.classLoader.getResourceAsStream("abcde/isa.yaml"),object :TypeReference<Isa>(){})
//        println(json.writeValueAsString(isa))
//        isa.groups.forEach { ig ->
//            ig.instructions.forEach {i ->
//                (0 until i.format.size).forEach{
//                    println(Inst.fromInstructionBean(ig,i,it).format)
//
//                }
//            }
//        }
//    }
//}