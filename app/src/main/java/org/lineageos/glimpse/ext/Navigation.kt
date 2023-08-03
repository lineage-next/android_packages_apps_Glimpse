/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.ext

import org.lineageos.glimpse.models.Album
import org.lineageos.glimpse.models.Media
import org.lineageos.glimpse.utils.CommonNavigationArguments

const val KEY_ALBUM = "album"
const val KEY_MEDIA = "media"
const val KEY_POSITION = "position"

fun createNavigationBundle(
    album: Album?,
    media: Media,
    position: Int,
) = CommonNavigationArguments().toBundle().apply {
    putParcelable(KEY_ALBUM, album)
    putParcelable(KEY_MEDIA, media)
    putInt(KEY_POSITION, position)
}