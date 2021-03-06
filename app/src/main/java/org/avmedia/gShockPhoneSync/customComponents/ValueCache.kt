/*
 * Created by Ivo Zivkov (izivkov@gmail.com) on 2022-04-16, 9:16 a.m.
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 2022-04-16, 9:16 a.m.
 */

package org.avmedia.gShockPhoneSync.customComponents

object ValueCache {
    private data class Value(val value: String, var isSet: Boolean = false)

    private val valueMap = HashMap<String, Value>()

    fun put(name: String, value: String) {
        valueMap[name] = Value(value, true)
    }

    fun get(name: String): String? {
        return valueMap[name]?.value
    }

    fun remove(name: String) {
        valueMap.remove(name)
    }

    fun isSet(name: String): Boolean {
        val value = valueMap[name] ?: return false
        return value.isSet
    }

    fun clear() {
        valueMap.clear()
    }
}