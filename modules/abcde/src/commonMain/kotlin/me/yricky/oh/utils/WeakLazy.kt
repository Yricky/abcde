package me.yricky.oh.utils

import java.lang.ref.WeakReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class WeakLazy<T>(private val initializer: () -> T) : ReadOnlyProperty<Any?, T> {
    private var weakRef = WeakReference<T>(null)
    private val lock = Any()

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        var instance = weakRef.get()
        if (instance == null) {
            synchronized(lock) {
                instance = weakRef.get()
                if (instance == null) {
                    instance = initializer()
                    weakRef = WeakReference(instance)
                }
            }
        }
        return instance!!
    }
}