/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.ext

import android.database.Cursor

fun <T> Cursor?.mapEachRow(
    projection: Array<String>,
    mapping: (Cursor, Array<Int>, Iterator<Int>) -> T,
) = this?.use {
    if (!moveToFirst()) {
        return@use emptyList<T>()
    }

    val indexCache = projection.map {
        getColumnIndexOrThrow(it)
    }.toTypedArray()
    val indexGenerator = generateSequence(0) {
        if (it >= indexCache.size - 1) null
        else it + 1
    }

    val data = mutableListOf<T>()
    do {
        val iterator = indexGenerator.iterator()
        data.add(mapping(this, indexCache, iterator))
        if (iterator.hasNext()) throw IllegalStateException("Iterator should be empty")
    } while (moveToNext())

    data.toList()
} ?: emptyList()
