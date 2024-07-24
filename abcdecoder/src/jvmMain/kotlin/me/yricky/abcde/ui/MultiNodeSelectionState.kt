package me.yricky.abcde.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import kotlin.math.max
import kotlin.math.min

class MultiNodeSelectionState {

    var selectedFrom:SelectionBound? by mutableStateOf(null)
    var selectedTo:SelectionBound? by mutableStateOf(null)

    @JvmInline
    value class SelectionBound(private val innerValue:Long){
        val index:Int get() = innerValue.ushr(32).toInt()
        val offset:Int get() = innerValue.and(0x7fffffffL).toInt()

        operator fun compareTo(that:SelectionBound):Int{
            return innerValue.compareTo(that.innerValue)
        }

        companion object{
            fun from(index:Int,offset:Int):SelectionBound{
                return SelectionBound(index.toLong().shl(32).or(offset.toLong()).and(0x7fff_ffff_7fff_ffffL))
            }
        }
    }
}

@Composable
fun MultiNodeSelectionState.rememberSelectionChange() = remember(this,selectedFrom,selectedTo) {
    val f = selectedFrom
    val t = selectedTo
    if(f == null || t == null){
        null
    } else if(f <= t) {
        Pair(f,t)
    } else {
        Pair(t,f)
    }
}


class MultiNodeSelectionScope{
    class SelectionNode(
        val index: Int,
        val layoutResult: () -> TextLayoutResult?,
        val text:AnnotatedString
    )
    val multiNodeSelectionState = MultiNodeSelectionState()
    internal val selectableMap = mutableMapOf<LayoutCoordinates,SelectionNode>()

    @Composable
    fun Modifier.withMultiNodeSelection(
        layoutResult:() -> TextLayoutResult?,
        text: AnnotatedString,
        index:Int
    ) = onPlaced {
        selectableMap[it] = SelectionNode(index,layoutResult, text)
//        println("($index) -> [${it.localToWindow(Offset.Zero)},${it.size}]")
    }.pointerHoverIcon(PointerIcon.Text)
}


@Composable
fun MultiNodeSelectionContainer(
    focusRequester: FocusRequester = remember { FocusRequester() },
    content:@Composable MultiNodeSelectionScope.()->Unit
){
    val mnScope = remember { MultiNodeSelectionScope() }
    var lc:LayoutCoordinates? by remember {
        mutableStateOf(null)
    }
    Box(Modifier.onKeyEvent {
        if(it.key == Key.A && it.isMetaPressed && it.type == KeyEventType.KeyDown){
            println("hit A")
            true
        } else false
    }.focusRequester(focusRequester).focusable(true).onPlaced { lc = it }.pointerInput(mnScope){
        awaitPointerEventScope {
            while (true){
                val pointerEvent = awaitPointerEvent(PointerEventPass.Main)
                if(!pointerEvent.buttons.isPrimaryPressed){
                    continue
                }
                val coordinates = lc ?: continue
                val position = pointerEvent.changes.firstOrNull()?.position ?: continue
                val windowOffset = coordinates.localToWindow(position)

                val state = mnScope.multiNodeSelectionState
                val iterator = mnScope.selectableMap.iterator()
                while (iterator.hasNext()){
                    val (layoutCoordinates,node) = iterator.next()
                    if(!layoutCoordinates.isAttached){
                        iterator.remove()
                    } else {
                        val size = layoutCoordinates.size
                        val localOff = layoutCoordinates.windowToLocal(windowOffset)
                        val index = node.index
                        if(localOff.x > 0 && localOff.y > 0 && localOff.x < size.width && localOff.y < size.height){
                            println("pointerAt:($index)$localOff")
                            val layoutResult = node.layoutResult() ?: break
                            if (pointerEvent.type == PointerEventType.Move && pointerEvent.buttons.isPrimaryPressed) {
                                state.selectedTo = MultiNodeSelectionState.SelectionBound.from(
                                    index,layoutResult.getOffsetForPosition(localOff)
                                )
                            } else if(pointerEvent.type == PointerEventType.Press && pointerEvent.buttons.isPrimaryPressed){
                                focusRequester.requestFocus()
                                state.selectedFrom = MultiNodeSelectionState.SelectionBound.from(
                                    index,layoutResult.getOffsetForPosition(localOff)
                                )
                                state.selectedTo = null
                            }
                            break
                        }
                    }
                }

            }

        }
    }){ mnScope.content() }
}

typealias SelectionRange = Pair<MultiNodeSelectionState.SelectionBound,MultiNodeSelectionState.SelectionBound>
val SelectionRange.start get() = first
val SelectionRange.endExclusive get() = second

fun SelectionRange.rangeOf(index:Int,str:CharSequence):PackedIntRange? = if (start.index > index || endExclusive.index < index) null else {
    if(start.index == index && endExclusive.index == index){
        PackedIntRange(min(start.offset,endExclusive.offset), max(start.offset,endExclusive.offset).coerceAtMost(str.length))
    } else if(start.index == index){
        PackedIntRange(start.offset,str.length)
    } else if(endExclusive.index == index){
        PackedIntRange(0,endExclusive.offset.coerceAtMost(str.length))
    } else {
        PackedIntRange(0,str.length)
    }
}


@JvmInline
value class PackedIntRange(private val value:Long){
    val start:Int get() = value.ushr(32).toInt()
    val endExclusive:Int get() = value.and(0xffff_ffffL).toInt()

    fun isEmpty() = start >= endExclusive
}
fun PackedIntRange(start:Int,endExclusive:Int): PackedIntRange {
    return PackedIntRange(start.toLong().shl(32).or(endExclusive.toLong()))
}