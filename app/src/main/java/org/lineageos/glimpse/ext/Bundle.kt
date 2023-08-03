/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.ext

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import org.lineageos.glimpse.fragments.InfiniteMediaViewerFragment
import org.lineageos.glimpse.models.Album
import org.lineageos.glimpse.models.Media
import org.lineageos.glimpse.utils.CommonNavigationArguments
import kotlin.reflect.KClass

fun <T : Parcelable> Bundle.getParcelable(key: String?, clazz: KClass<T>) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, clazz.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelable(key)
    }
