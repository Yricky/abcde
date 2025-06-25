package me.yricky.oh.common

/**
 * map操作非常小时，可以节省内存开销
 */
class LazyMapList<T, R>(private val list: List<T>, private val map: (T) -> R) : List<R> {
    override val size: Int get() = list.size

    override fun contains(element: R): Boolean {
        for (e in list) {
            if (map(e) == element) return true
        }
        return false
    }

    override fun containsAll(elements: Collection<R>): Boolean {
        val remaining = elements.toMutableSet()
        for (e in list) {
            val mapped = map(e)
            remaining.remove(mapped)
            if (remaining.isEmpty()) return true
        }
        return false
    }

    override fun get(index: Int): R = map(list[index])

    override fun indexOf(element: R): Int {
        for ((i, e) in list.withIndex()) {
            if (map(e) == element) return i
        }
        return -1
    }

    override fun isEmpty(): Boolean = list.isEmpty()

    override fun iterator(): Iterator<R> = object : Iterator<R> {
        private val iter = list.iterator()
        override fun hasNext(): Boolean = iter.hasNext()
        override fun next(): R = map(iter.next())
    }

    override fun lastIndexOf(element: R): Int {
        for (i in list.indices.reversed()) {
            if (map(list[i]) == element) return i
        }
        return -1
    }

    override fun listIterator(): ListIterator<R> = listIterator(0)

    override fun listIterator(index: Int): ListIterator<R> = object : ListIterator<R> {
        private val iter = list.listIterator(index)
        override fun hasNext(): Boolean = iter.hasNext()
        override fun next(): R = map(iter.next())
        override fun hasPrevious(): Boolean = iter.hasPrevious()
        override fun previous(): R = map(iter.previous())
        override fun nextIndex(): Int = iter.nextIndex()
        override fun previousIndex(): Int = iter.previousIndex()
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<R> {
        return LazyMapList(list.subList(fromIndex, toIndex), map)
    }

    override fun toString(): String = joinToString(", ", "[", "]")
}