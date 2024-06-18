package me.yricky.common

class TreeStruct<T>(
    val source:Iterable<T>,
    val pathOf:(T) -> String,
    val pathSeparatorChar:Char = '/'
) {
    val rootNode:TreeNode<T> = MutableTreeNode("",null)

    private val pathSegCache = mutableMapOf<String,List<String>>()
    private fun getSeg(path:String):List<String>{
        return pathSegCache[path] ?: path.split(pathSeparatorChar).also {
            pathSegCache[path] = it
        }
    }

    init {
        source.forEach {
            var node = rootNode as MutableTreeNode<T>
            val iterator = getSeg(pathOf(it)).iterator()
            while (iterator.hasNext()){
                val nxt = iterator.next()
                if(iterator.hasNext()){
                    node = node.getChildNode(nxt) ?: return@forEach
                } else {
                    node.setChildValue(nxt,it)
                }
            }
        }
    }

    sealed class Node<T>(val pathSeg:String,val parent:TreeNode<T>?){
        override fun equals(other: Any?): Boolean {
            if(other == null){
                return false
            } else if(javaClass != other.javaClass){
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
        abstract val children:Map<String,Node<T>>
    }
    private class MutableTreeNode<T>(pathSeg: String, parent: TreeNode<T>?) : TreeNode<T>(pathSeg, parent){
        val mutableChildren = mutableMapOf<String,Node<T>>()
        override val children: Map<String, Node<T>> get() = mutableChildren

        fun getChildNode(childPathSeg:String):MutableTreeNode<T>?{
            return when(val node = mutableChildren[childPathSeg]){
                is TreeNode<T> -> node as MutableTreeNode
                is LeafNode<T> -> null
                null -> MutableTreeNode<T>(childPathSeg,this).also {
                    mutableChildren[childPathSeg] = it
                }
            }
        }

        fun setChildValue(childPathSeg: String, value:T):Boolean{
            return when(val node = mutableChildren[childPathSeg]){
                null -> {
                    mutableChildren[childPathSeg] = LeafNode(childPathSeg, value,this)
                    true
                }
                else -> false
            }
        }
    }
    class LeafNode<T>(path: String,val value:T, parent: TreeNode<T>?) : Node<T>(path, parent)
}