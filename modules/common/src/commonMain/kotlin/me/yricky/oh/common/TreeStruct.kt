package me.yricky.oh.common

import java.util.HashMap
import java.util.LinkedHashMap
import java.util.TreeMap

class TreeStruct<T>(
    source:Iterable<Pair<Iterable<String>,T>>,
    pathSeparator:Char = PATH_SEPARATOR_CHAR,
    sortByPath:Boolean = true
) {
    companion object{
        const val PATH_SEPARATOR_CHAR:Char = '/'
    }

    val rootNode:TreeNode<T> = MutableTreeNode("",null, sortByPath)

    val pathMap :Map<String,LeafNode<T>>

    init {
        val map = HashMap<String,LeafNode<T>>()
        source.forEach {
            var node = rootNode as MutableTreeNode<T>
            val path = it.first
            val pathStr = path.joinToString(pathSeparator.toString())
            val iterator = path.iterator()
            while (iterator.hasNext()){
                val nxt = iterator.next()
                if(iterator.hasNext()){
                    node = node.getChildNode(nxt)
                } else {
                    node.setChildValue(nxt,it.second)?.let { l ->
                        map[pathStr] = l
                    } ?: let {
                        println("already has this:${path}")
                    }
                }
            }
        }
        pathMap = map
    }

    sealed class Node<T>(
        val pathSeg:String,
        val parent:TreeNode<T>?
    ){
        fun path(): Sequence<String> = sequence {
            parent?.path()?.let { yieldAll(it) }
            yield(pathSeg)
        }

        /**
         * 与叶子节点path相同的非叶子节点，或反过来
         */
        abstract fun relevantNode(): Node<T>?

        override fun equals(other: Any?): Boolean {
            if(other == null){
                return false
            } else if(this::class != other::class){
                return false
            } else {
                val that = other as Node<*>
                if(pathSeg != that.pathSeg){
                    return false
                }
                if(parent != that.parent){
                    return false
                }
                if(parent == null){
                    return hashCode() == that.hashCode()
                }
            }
            return true
        }

        override fun hashCode(): Int {
            var result = pathSeg.hashCode()
            result = 31 * result + (parent?.hashCode() ?: super.hashCode())
            return result
        }
    }
    abstract class TreeNode<T>(pathSeg: String, parent: TreeNode<T>?) : Node<T>(pathSeg, parent){
        abstract val treeChildren:Map<String,TreeNode<T>>
        abstract val leafChildren:Map<String,LeafNode<T>>

        override fun relevantNode(): LeafNode<T>? = parent?.leafChildren?.get(pathSeg)
    }
    private class MutableTreeNode<T>(pathSeg: String, parent: TreeNode<T>?, val sortByPath: Boolean) : TreeNode<T>(pathSeg, parent){
        val mutableChildren:MutableMap<String,TreeNode<T>> = if (sortByPath) TreeMap() else LinkedHashMap()
        override val treeChildren: Map<String, TreeNode<T>> get() = mutableChildren
        val mutableLeafChildren: MutableMap<String,LeafNode<T>> = if (sortByPath) TreeMap() else LinkedHashMap()
        override val leafChildren: Map<String, LeafNode<T>> get() = mutableLeafChildren

        fun getChildNode(childPathSeg:String):MutableTreeNode<T>{
            return when(val node = mutableChildren[childPathSeg]){
                is TreeNode<T> -> node as MutableTreeNode
                else -> MutableTreeNode(childPathSeg,this, sortByPath).also {
                    mutableChildren[childPathSeg] = it
                }
            }
        }

        fun setChildValue(childPathSeg: String, value:T):LeafNode<T>?{
            return when(val node = mutableLeafChildren[childPathSeg]){
                null -> {
                    LeafNode(childPathSeg, value,this).also {
                        mutableLeafChildren[childPathSeg] = it
                    }
                }
                else -> null
            }
        }
    }
    class LeafNode<T>(pathSeg: String, val value:T, parent: TreeNode<T>?) : Node<T>(pathSeg, parent) {
        override fun relevantNode(): TreeNode<T>? = parent?.treeChildren?.get(pathSeg)
    }
}

fun <T> TreeStruct(
    source:Iterable<T>,
    pathOf:(T) -> String,
    pathSeparator:Char = TreeStruct.PATH_SEPARATOR_CHAR,
):TreeStruct<T> {
    return TreeStruct(source.asSequence().map {
        Pair(pathOf(it).split(pathSeparator),it)
    }.asIterable())
}