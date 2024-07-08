package me.yricky.oh.common

typealias DataAndNextOff<T> = Pair<T,Int>
val <T> DataAndNextOff<T>.value:T get() = first
val <T> DataAndNextOff<T>.nextOffset:Int get() = second