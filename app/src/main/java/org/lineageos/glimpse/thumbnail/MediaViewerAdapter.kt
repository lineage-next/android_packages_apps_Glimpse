/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.thumbnail

import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import org.lineageos.glimpse.R
import org.lineageos.glimpse.models.Media
import org.lineageos.glimpse.models.MediaType
import java.util.Date

class MediaViewerAdapter(
    private val exoPlayer: ExoPlayer
) : BaseCursorAdapter<MediaViewerAdapter.MediaViewHolder>() {
    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = getIdFromMediaStore(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MediaViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.media_view, parent, false)
    )

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        getMediaFromMediaStore(position)?.let { holder.bind(it, exoPlayer) }
    }

    override fun onViewAttachedToWindow(holder: MediaViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.attachPlayer()
    }

    override fun onViewDetachedFromWindow(holder: MediaViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.detachPlayer()
    }

    fun getMediaFromMediaStore(position: Int): Media? {
        val cursor = cursor ?: return null

        val idIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
        val mediaTypeIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)
        val dateAddedIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)

        cursor.moveToPosition(position)

        val id = cursor.getLong(idIndex)
        val mediaType = cursor.getInt(mediaTypeIndex)
        val dateAdded = cursor.getLong(dateAddedIndex)

        return Media(id, MediaType.fromMediaStoreValue(mediaType), Date(dateAdded * 1000))
    }

    private fun getIdFromMediaStore(position: Int): Long {
        val cursor = cursor ?: return 0
        val idIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
        cursor.moveToPosition(position)
        return cursor.getLong(idIndex)
    }

    class MediaViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {
        // Views
        private val imageView = view.findViewById<ImageView>(R.id.imageView)
        private val playerView = view.findViewById<PlayerView>(R.id.playerView)

        private lateinit var media: Media
        private lateinit var exoPlayer: ExoPlayer

        fun bind(media: Media, exoPlayer: ExoPlayer) {
            this.media = media
            this.exoPlayer = exoPlayer

            when (media.mediaType) {
                MediaType.IMAGE -> {
                    imageView.load(media.externalContentUri)
                }

                MediaType.VIDEO -> {}
            }

            imageView.isVisible = media.mediaType == MediaType.IMAGE
            playerView.isVisible = media.mediaType == MediaType.VIDEO
        }

        fun attachPlayer() {
            if (media.mediaType == MediaType.VIDEO) {
                playerView.player = exoPlayer
            }
        }

        fun detachPlayer() {
            if (media.mediaType == MediaType.VIDEO) {
                playerView.player = null
            }
        }
    }
}
