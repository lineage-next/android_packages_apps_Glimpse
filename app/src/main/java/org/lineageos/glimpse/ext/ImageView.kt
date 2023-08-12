/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.ext

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory.Builder
import org.lineageos.glimpse.GlideApp
import org.lineageos.glimpse.R
import org.lineageos.glimpse.models.Media

fun ImageView.load(media: Media, onLoadingFinished: () -> Unit = {}) {
    val listener = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            onLoadingFinished()
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            onLoadingFinished()
            return false
        }

    }
    val requestOptions = RequestOptions.placeholderOf(R.drawable.thumbnail_placeholder)
        .dontTransform()
    val factory = Builder().setCrossFadeEnabled(true).build()
    val transition = DrawableTransitionOptions().crossFade(factory)
    GlideApp.with(this)
        .load(media.externalContentUri)
        .signature(media.signature())
        .apply(requestOptions)
        .listener(listener)
        .transition(transition)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}
