package me.yricky.oh.utils

class LruMap<K,V>: java.util.LinkedHashMap<K,V>() {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return (size > 10000)
    }
}