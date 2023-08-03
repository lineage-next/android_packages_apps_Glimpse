/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.thumbnail

import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import org.lineageos.glimpse.R
import org.lineageos.glimpse.models.Media
import org.lineageos.glimpse.models.MediaType
import java.text.SimpleDateFormat
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
        getMediaFromMediaStore(position)?.let {
            holder.bind(it, exoPlayer)
        }
    }

    private fun getIdFromMediaStore(position: Int): Long {
        val cursor = cursor ?: return 0
        val idIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
        cursor.moveToPosition(position)
        return cursor.getLong(idIndex)
    }

    private fun getMediaFromMediaStore(position: Int): Media? {
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

    class MediaViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {
        // Views
        private val adjustButton = view.findViewById<ImageButton>(R.id.adjustButton)
        private val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        private val deleteButton = view.findViewById<ImageButton>(R.id.deleteButton)
        private val favoriteButton = view.findViewById<ImageButton>(R.id.favoriteButton)
        private val imageView = view.findViewById<ImageView>(R.id.imageView)
        private val playerView = view.findViewById<PlayerView>(R.id.playerView)
        private val shareButton = view.findViewById<ImageButton>(R.id.shareButton)
        private val timeTextView = view.findViewById<TextView>(R.id.timeTextView)

        fun bind(media: Media, exoPlayer: ExoPlayer) {
            playerView.player = exoPlayer

            when (media.mediaType) {
                MediaType.IMAGE -> {
                    imageView.load(media.externalContentUri)
                }

                MediaType.VIDEO -> {
                    with(exoPlayer) {
                        setMediaItem(MediaItem.fromUri(media.externalContentUri))
                        seekTo(C.TIME_UNSET)
                        prepare()
                    }
                }
            }

            dateTextView.text = dateFormatter.format(media.dateAdded)
            timeTextView.text = timeFormatter.format(media.dateAdded)

            imageView.isVisible = media.mediaType == MediaType.IMAGE
            playerView.isVisible = media.mediaType == MediaType.VIDEO
        }

        companion object {
            private val dateFormatter = SimpleDateFormat.getDateInstance()
            private val timeFormatter = SimpleDateFormat.getTimeInstance()
        }
    }
}
