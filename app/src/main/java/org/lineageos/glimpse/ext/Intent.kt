/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.ext

import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION_CODES
import android.provider.Settings
import androidx.annotation.RequiresApi
import org.lineageos.glimpse.models.Media
import org.lineageos.glimpse.models.MediaType.IMAGE
import org.lineageos.glimpse.models.MediaType.VIDEO

fun Intent.shareIntent(vararg medias: Media) = apply {
    action = Intent.ACTION_SEND_MULTIPLE
    putParcelableArrayListExtra(
        Intent.EXTRA_STREAM,
        medias.map { it.externalContentUri }.toCollection(ArrayList())
    )
    type = when {
        medias.all { it.mediaType == IMAGE } -> "image/*"
        medias.all { it.mediaType == VIDEO } -> "video/*"
        else -> "*/*"
    }
    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
}

fun Intent.editIntent(media: Media) = apply {
    action = Intent.ACTION_EDIT
    setDataAndType(media.externalContentUri, media.mimeType)
    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
}

@RequiresApi(VERSION_CODES.S)
fun Intent.requestManagedMedia(packageName: String) = apply {
    action = Settings.ACTION_REQUEST_MANAGE_MEDIA
    data = Uri.fromParts("package", packageName, null)
}
