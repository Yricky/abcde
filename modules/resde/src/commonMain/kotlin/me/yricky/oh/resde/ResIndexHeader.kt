package me.yricky.oh.resde

class ResIndexHeader(
    val version:ByteArray, //size = 128
    val fileSize:Int,
    val limitKeyConfigCount:Int
)