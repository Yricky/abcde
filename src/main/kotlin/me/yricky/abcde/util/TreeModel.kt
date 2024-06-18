package me.yricky.abcde.util

import me.yricky.common.TreeStruct

class TreeModel<T>(
    val tree:TreeStruct<T>
) {

    val expandNodes get():Set<TreeStruct.TreeNode<T>> = _expandNodes

    fun toggleExpand(node: TreeStruct.TreeNode<T>){
        if(expandNodes.contains(node)){
            _expandNodes.remove(node)
        } else {
            var _node: TreeStruct.TreeNode<T>? = node
            while (_node != null){
                _expandNodes.add(_node)
                _node = _node.parent
            }
        }
    }

    fun foldAll(){
        _expandNodes.clear()
    }

    fun isExpand(node: TreeStruct.TreeNode<T>) = expandNodes.contains(node)

    fun buildFlattenList(filter:String = ""):List<Pair<Int, TreeStruct.Node<T>>>{
        val list = mutableListOf<Pair<Int, TreeStruct.Node<T>>>()
        if(filter.isEmpty()){
            innerBuildFlattenList(tree.rootNode,0,list)
        } else {
            innerBuildFlattenList(tree.rootNode,0,list,filter)
        }

        return list
    }

    private fun innerBuildFlattenList(parentNode:TreeStruct.TreeNode<T>,indent:Int,list:MutableList<Pair<Int,TreeStruct.Node<T>>>){
        parentNode.children.forEach { _, n ->
            list.add(Pair(indent,n))
            if(expandNodes.contains(n)){
                innerBuildFlattenList(n as TreeStruct.TreeNode<T>,indent + 1, list)
            }
        }
    }

    private fun innerBuildFlattenList(parentNode:TreeStruct.TreeNode<T>, indent:Int, list:MutableList<Pair<Int,TreeStruct.Node<T>>>, filter: String, findInParent:Boolean = false):Boolean{
        var shouldAddThis = false
        parentNode.children.forEach { _, n ->
            val size = list.size
            val matchThis = findInParent || n.pathSeg.contains(filter)
            val findInChild = if(n is TreeStruct.TreeNode) innerBuildFlattenList(n,indent + 1, list,filter, matchThis) else false
            if(matchThis || findInChild){
                list.add(size,Pair(indent,n))
                shouldAddThis = true
            }
        }
        return shouldAddThis
    }

    private val _expandNodes = mutableSetOf<TreeStruct.TreeNode<T>>()
}