/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.ext

import android.database.Cursor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun <T> Flow<Cursor?>.mapEachRow(
    projection: Array<String>,
    mapping: (Cursor, Array<Int>, Iterator<Int>) -> T,
) = map { it.mapEachRow(projection, mapping) }
