package me.yricky.oh.resde

class ResourceItem(
    val fileName:String,
    val keyParams:List<LimitKeyConfig.KeyParam>,
    val resType:ResType,
    val data:String
){
    val limitKey by lazy {
        if(keyParams.isEmpty()){
            "base"
        } else {
            keyParams.fold("") { s, p ->
                if (p.keyType == LimitKeyConfig.KeyParam.TYPE_MNC || p.keyType == LimitKeyConfig.KeyParam.TYPE_REGION) {
                    "${s}_$p"
                } else {
                    "${s}-${p}"
                }
            }.removePrefix("-")
        }
    }
}