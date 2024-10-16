package me.yricky.oh.common

import java.util.HashMap
import java.util.LinkedHashMap
import java.util.TreeMap

class TreeStruct<T>(
    source:Iterable<Pair<String,T>>,
    sortByPath:Boolean = true
) {
    companion object{
        private const val PATH_SEPARATOR_CHAR:Char = '/'
    }

    val rootNode:TreeNode<T> = MutableTreeNode("",null, sortByPath)

    val pathMap :Map<String,LeafNode<T>>

    init {
        val map = HashMap<String,LeafNode<T>>()
        source.forEach {
            var node = rootNode as MutableTreeNode<T>
            val path = it.first
            val iterator = path.split(PATH_SEPARATOR_CHAR).iterator()
            while (iterator.hasNext()){
                val nxt = iterator.next()
                if(iterator.hasNext()){
                    node = node.getChildNode(nxt)
                } else {
                    node.setChildValue(path,nxt,it.second)?.let { l ->
                        map[path] = l
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
        abstract val path:String
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
    }
    private class MutableTreeNode<T>(pathSeg: String, parent: TreeNode<T>?, val sortByPath: Boolean) : TreeNode<T>(pathSeg, parent){
        val mutableChildren:MutableMap<String,TreeNode<T>> = if (sortByPath) TreeMap() else LinkedHashMap()
        override val treeChildren: Map<String, TreeNode<T>> get() = mutableChildren
        val mutableLeafChildren: MutableMap<String,LeafNode<T>> = if (sortByPath) TreeMap() else LinkedHashMap()
        override val leafChildren: Map<String, LeafNode<T>> get() = mutableLeafChildren

        override val path: String by lazy {
            val sb = StringBuilder()
            sb.append(pathSeg)
            parent?.let { p ->
                sb.append(p.path)
                sb.append(PATH_SEPARATOR_CHAR)
            }
            sb.append(pathSeg)
            sb.toString()
        }

        fun getChildNode(childPathSeg:String):MutableTreeNode<T>{
            return when(val node = mutableChildren[childPathSeg]){
                is TreeNode<T> -> node as MutableTreeNode
                else -> MutableTreeNode(childPathSeg,this, sortByPath).also {
                    mutableChildren[childPathSeg] = it
                }
            }
        }

        fun setChildValue(path: String,childPathSeg: String, value:T):LeafNode<T>?{
            return when(val node = mutableLeafChildren[childPathSeg]){
                null -> {
                    LeafNode(path,childPathSeg, value,this).also {
                        mutableLeafChildren[childPathSeg] = it
                    }
                }
                else -> null
            }
        }
    }
    class LeafNode<T>(override val path: String,pathSeg: String, val value:T, parent: TreeNode<T>?) : Node<T>(pathSeg, parent)
}

fun <T> TreeStruct(
    source:Iterable<T>,
    pathOf:(T) -> String,
):TreeStruct<T> {
    return TreeStruct(source.asSequence().map { Pair(pathOf(it),it) }.asIterable())
}