package me.yricky.oh.resde

class IdSet(
    count:Int,
    idOffsetMap: Map<Int,Int>
) {
    companion object{
        const val TAG = 0x53534449 // "IDSS"
    }
}