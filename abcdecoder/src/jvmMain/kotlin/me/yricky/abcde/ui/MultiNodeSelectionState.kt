package me.yricky.abcde.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import me.yricky.abcde.desktop.DesktopUtils
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
            val Zero = from(0,0)
            fun from(index:Int,offset:Int):SelectionBound{
                return SelectionBound(index.toLong().shl(32).or(offset.toLong()).and(0x7fff_ffff_7fff_ffffL))
            }
        }
    }
}

@Composable
fun MultiNodeSelectionState.rememberSelectionChange():SelectionRange? = remember(this,selectedFrom,selectedTo) {
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

sealed class TextAction{
    class Click(val location:MultiNodeSelectionState.SelectionBound):TextAction()
    class DoubleClick(val location:MultiNodeSelectionState.SelectionBound):TextAction()
    class SecClick(val location:MultiNodeSelectionState.SelectionBound):TextAction()
    class Copy(val range:SelectionRange):TextAction()
    data object SelectAll:TextAction()
    data object Search:TextAction()
}


class MultiNodeSelectionScope{
    class SelectionNode(
        val index: Int,
        val layoutResult: () -> TextLayoutResult?,
    )
    val multiNodeSelectionState = MultiNodeSelectionState()
    internal val selectableMap = mutableMapOf<LayoutCoordinates,SelectionNode>()

    /**
     * Triple<[System.currentTimeMillis],[LayoutCoordinates.localToWindow],[TextLayoutResult.getOffsetForPosition]>
     */
    internal var lastPrimaryClick:Triple<Long,Offset,MultiNodeSelectionState.SelectionBound>? = null

    internal val textActionFlowMut = MutableSharedFlow<TextAction>(0,1,BufferOverflow.DROP_OLDEST)
    val textActionFlow = textActionFlowMut.asSharedFlow()


    @Composable
    fun Modifier.withMultiNodeSelection(
        layoutResult:() -> TextLayoutResult?,
        index:Int
    ) = onPlaced {
        selectableMap[it] = SelectionNode(index,layoutResult)
//        println("($index) -> [${it.localToWindow(Offset.Zero)},${it.size}]")
    }.pointerHoverIcon(PointerIcon.Text)
}

val KeyEvent.isCtrlPressedCompat get() = (DesktopUtils.isMacos && isMetaPressed) || (isCtrlPressed && !DesktopUtils.isMacos)

@Composable
fun MultiNodeSelectionContainer(
    focusRequester: FocusRequester = remember { FocusRequester() },
    content:@Composable MultiNodeSelectionScope.()->Unit
){
    val mnScope = remember { MultiNodeSelectionScope() }
    var lc:LayoutCoordinates? by remember {
        mutableStateOf(null)
    }
    val range = mnScope.multiNodeSelectionState.rememberSelectionChange()
    Box(Modifier.onKeyEvent {
        if(it.isCtrlPressedCompat && it.type == KeyEventType.KeyDown){
            when(it.key){
                Key.A -> mnScope.textActionFlowMut.tryEmit(TextAction.SelectAll)
                Key.C -> range?.let { mnScope.textActionFlowMut.tryEmit(TextAction.Copy(it)) }
                Key.F -> mnScope.textActionFlowMut.tryEmit(TextAction.Search)
            }
            true
        } else false
    }.focusRequester(focusRequester).focusable(true).onPlaced { lc = it }.pointerInput(mnScope){
        awaitPointerEventScope {
            while (true){
                val pointerEvent = awaitPointerEvent(PointerEventPass.Main)
                if((!pointerEvent.buttons.areAnyPressed) && (pointerEvent.type != PointerEventType.Release)){
                    continue
                }
                val coordinates = lc ?: continue
                val position = pointerEvent.changes.firstOrNull()?.position ?: continue
                val windowOffset = coordinates.localToWindow(position)

                val state = mnScope.multiNodeSelectionState
                val iterator = mnScope.selectableMap.iterator()
                var handled = false
                while (iterator.hasNext()){
                    val (layoutCoordinates,node) = iterator.next()
                    if(!layoutCoordinates.isAttached){
                        iterator.remove()
                    } else {
                        val size = layoutCoordinates.size
                        val localOff = layoutCoordinates.windowToLocal(windowOffset)
                        val index = node.index
                        if(localOff.inBound(size)){
//                            println("pointerAt:($index)$localOff")
                            val layoutResult = node.layoutResult() ?: break
                            val localTextBound = MultiNodeSelectionState.SelectionBound.from(
                                index,layoutResult.getOffsetForPosition(localOff)
                            )
                            val currTime = System.currentTimeMillis()
                            val lpc = mnScope.lastPrimaryClick
                            if (pointerEvent.type == PointerEventType.Move && pointerEvent.buttons.isPrimaryPressed) {
                                //拖动
                                if(state.selectedFrom == null){
                                    state.selectedFrom = localTextBound
                                } else {
                                    state.selectedTo = localTextBound
                                }
                            } else if(pointerEvent.type == PointerEventType.Press && pointerEvent.buttons.isPrimaryPressed){
                                //左键点击
                                focusRequester.requestFocus()
                                if(pointerEvent.keyboardModifiers.isShiftPressed && state.selectedFrom != null){
                                    state.selectedTo = localTextBound
                                }else {
                                    if(lpc != null && currTime - lpc.first < 200 && localTextBound == lpc.third){
                                        mnScope.textActionFlowMut.tryEmit(TextAction.DoubleClick(localTextBound))
                                        mnScope.lastPrimaryClick = null
                                    } else {
                                        mnScope.lastPrimaryClick = Triple(currTime,windowOffset,localTextBound)
                                    }
                                    state.selectedFrom = localTextBound
                                    state.selectedTo = null
                                }
                            } else if (pointerEvent.type == PointerEventType.Press && pointerEvent.buttons.isSecondaryPressed){
                                //右键点击
                                mnScope.textActionFlowMut.tryEmit(TextAction.SecClick(localTextBound))
                            } else if(pointerEvent.type == PointerEventType.Release) {
                                //释放点击
                                if(lpc != null && currTime - lpc.first < 200 && localTextBound == lpc.third){
                                    mnScope.textActionFlowMut.tryEmit(TextAction.Click(localTextBound))
                                }
                            }
                            handled = true
                            break
                        }
                    }
                }
                if(pointerEvent.type == PointerEventType.Press &&
                    pointerEvent.buttons.isPrimaryPressed &&
                    !handled){
                    state.selectedFrom = null
                    state.selectedTo = null
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


fun Offset.inBound(bound: IntSize):Boolean{
    return x > 0 && y > 0 && x < bound.width && y < bound.height
}