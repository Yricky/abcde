package me.yricky.oh.common

/**
 * [arr]中的元素保证按照keyGen(arr)升序排列
 */
class BinarySearchMap<K: Comparable<K>,V>(private val arr: ArrayList<V>, val keyGen:(V)->K): Map<K,V> {
    override val entries: Set<Map.Entry<K, V>> get() = EntrySet()
    override val keys: Set<K> get() = KeySet()
    override val size: Int get() = arr.size
    override val values: Collection<V> get() = arr

    override fun containsKey(key: K): Boolean {
        return findIndex(key) >= 0
    }

    override fun containsValue(value: V): Boolean {
        // 值查找需要线性扫描，因为值没有排序
        return arr.any { it == value }
    }

    override fun get(key: K): V? {
        val index = findIndex(key)
        return if (index >= 0) arr[index] else null
    }

    override fun isEmpty(): Boolean = (arr.size == 0)

    // 二分查找核心实现
    private fun findIndex(key: K): Int {
        var low = 0
        var high = arr.size - 1

        while (low <= high) {
            val mid = (low + high) / 2
            val midKey = keyGen(arr[mid])

            when {
                midKey < key -> low = mid + 1
                midKey > key -> high = mid - 1
                else -> return mid
            }
        }
        return -1
    }

    /**
     * 插入新值并返回对应的键
     * 时间复杂度: O(n) 插入 + O(log n) 查找位置
     */
    fun insert(value: V): K {
        val key = keyGen(value)
        if(arr.isEmpty() || key > keyGen(arr.last())){
            arr.add(value)
        } else {
            findInsertionIndex(key, value)
        }
        return key
    }
    /**
     * 查找应插入的位置（二分查找）
     * 返回第一个大于或等于key的元素位置
     */
    private fun findInsertionIndex(key: K, value: V) {
        var low = 0
        var high = arr.size - 1

        while (low <= high) {
            val mid = (low + high) / 2
            val midKey = keyGen(arr[mid])

            when {
                midKey < key -> low = mid + 1
                midKey > key -> high = mid - 1
                else -> arr[mid] = value // 键已存在，插入相同位置（覆盖语义）
            }
        }
        arr.add(low,value)
    }

    inner class KeySet: Set<K>{
        override val size: Int get() = this@BinarySearchMap.size

        override fun contains(element: K): Boolean {
            return this@BinarySearchMap.containsKey(element)
        }

        override fun containsAll(elements: Collection<K>): Boolean {
            return elements.all { contains(it) }
        }

        override fun isEmpty(): Boolean = this@BinarySearchMap.isEmpty()

        override fun iterator(): Iterator<K> = object : Iterator<K> {
            private val iter = arr.iterator()
            override fun hasNext(): Boolean = iter.hasNext()
            override fun next(): K = keyGen(iter.next())
        }
    }

    inner class EntrySet: Set<Map.Entry<K,V>>{
        override val size: Int get() = this@BinarySearchMap.size

        override fun contains(element: Map.Entry<K, V>): Boolean {
            val value = get(element.key)
            return value != null && value == element.value
        }

        override fun containsAll(elements: Collection<Map.Entry<K, V>>): Boolean {
            return elements.all { contains(it) }
        }

        override fun isEmpty(): Boolean = this@BinarySearchMap.isEmpty()

        override fun iterator(): Iterator<Map.Entry<K, V>> = object : Iterator<Map.Entry<K, V>> {
            private val iter = arr.iterator()
            override fun hasNext(): Boolean = iter.hasNext()
            override fun next(): Map.Entry<K, V> = object : Map.Entry<K, V> {
                private val item = iter.next()
                override val key: K get() = keyGen(item)
                override val value: V get() = item
            }
        }
    }
}