package me.yricky.abcde.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.yricky.oh.common.TreeStruct

class TreeModel<T>(
    val tree: TreeStruct<T>
) {

    private val expandNodes get():Set<TreeStruct.TreeNode<T>> = _expandNodes

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

    fun buildFlattenList(filter:((TreeStruct.Node<T>) -> Boolean)? = null):List<Pair<Int, TreeStruct.Node<T>>>{
        val list = mutableListOf<Pair<Int, TreeStruct.Node<T>>>()
        if(filter == null){
            innerBuildFlattenList(tree.rootNode,0,list)
        } else {
            innerBuildFlattenList(tree.rootNode,0,list,filter)
        }

        return list
    }

    private fun innerBuildFlattenList(parentNode: TreeStruct.TreeNode<T>, indent:Int, list:MutableList<Pair<Int, TreeStruct.Node<T>>>){
        parentNode.leafChildren.forEach { (_, n) ->
            list.add(Pair(indent,n))
        }
        parentNode.treeChildren.forEach { (_, n) ->
            list.add(Pair(indent,n))
            if(expandNodes.contains(n)){
                innerBuildFlattenList(n,indent + 1, list)
            }
        }
    }

    private fun innerBuildFlattenList(parentNode: TreeStruct.TreeNode<T>, indent:Int, list:MutableList<Pair<Int, TreeStruct.Node<T>>>, filter: (TreeStruct.Node<T>) -> Boolean, findInParent:Boolean = false):Boolean{
        var shouldAddThis = false
        parentNode.leafChildren.forEach { (_, n) ->
            val size = list.size
            val matchThis = findInParent || filter(n)
            if(matchThis){
                list.add(size,Pair(indent,n))
                shouldAddThis = true
            }
        }
        parentNode.treeChildren.forEach { (_, n) ->
            val size = list.size
            val matchThis = findInParent || filter(n)
            val findInChild = innerBuildFlattenList(n,indent + 1, list,filter, matchThis)
            if(matchThis || findInChild){
                list.add(size,Pair(indent,n))
                shouldAddThis = true
            }
        }
        return shouldAddThis
    }

    private val _expandNodes = mutableSetOf<TreeStruct.TreeNode<T>>()
}

private fun JsonElement.asSequence(path:String = ""):Sequence<Pair<String,JsonPrimitive>> = sequence {
    when(this@asSequence){
        is JsonArray -> forEachIndexed { index: Int, jsonElement: JsonElement ->
            yieldAll(jsonElement.asSequence("$path[${size}]/[$index]"))
        }
        is JsonObject -> forEach { (key,ele)  ->
            yieldAll(ele.asSequence("$path{$size}/$key"))
        }
        is JsonPrimitive -> yield(Pair(path,this@asSequence))
    }
}

fun JsonElement.toTreeStruct():TreeStruct<JsonPrimitive> = TreeStruct(asSequence().asIterable(), false)