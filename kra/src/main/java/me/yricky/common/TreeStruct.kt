package me.yricky.common

class TreeStruct<T>(
    val source:Iterable<T>,
    val pathOf:(T) -> String,
    val pathSeparatorChar:Char = '/'
) {
    val rootNode:TreeNode<T> = MutableTreeNode("",null)
    //TODO 将展开的逻辑移到UI module中
    val expandNodes get():Set<TreeNode<T>> = _expandNodes

    fun toggleExpand(node: TreeNode<T>){
        if(expandNodes.contains(node)){
            _expandNodes.remove(node)
        } else {
            var _node:TreeNode<T>? = node
            while (_node != null){
                _expandNodes.add(_node)
                _node = _node.parent
            }
        }
    }

    fun foldAll(){
        _expandNodes.clear()
    }

    fun isExpand(node: TreeNode<T>) = expandNodes.contains(node)

    fun buildFlattenList(filter:String = ""):List<Pair<Int,Node<T>>>{
        val list = mutableListOf<Pair<Int,Node<T>>>()
        if(filter.isEmpty()){
            innerBuildFlattenList(rootNode,0,list)
        } else {
            innerBuildFlattenList(rootNode,0,list,filter)
        }

        return list
    }

    private fun innerBuildFlattenList(parentNode:TreeNode<T>,indent:Int,list:MutableList<Pair<Int,Node<T>>>){
        parentNode.children.forEach { _, n ->
            list.add(Pair(indent,n))
            if(expandNodes.contains(n)){
                innerBuildFlattenList(n as TreeNode<T>,indent + 1, list)
            }
        }
    }

    private fun innerBuildFlattenList(parentNode:TreeNode<T>, indent:Int, list:MutableList<Pair<Int,Node<T>>>, filter: String, findInParent:Boolean = false):Boolean{
        var shouldAddThis = false
        parentNode.children.forEach { _, n ->
            val size = list.size
            val matchThis = findInParent || n.pathSeg.contains(filter)
            val findInChild = if(n is TreeNode) innerBuildFlattenList(n,indent + 1, list,filter, matchThis) else false
            if(matchThis || findInChild){
                list.add(size,Pair(indent,n))
                shouldAddThis = true
            }
        }
        return shouldAddThis
    }

    private val _expandNodes = mutableSetOf<TreeNode<T>>()
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