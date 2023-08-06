/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.thumbnail

import android.database.Cursor
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import org.lineageos.glimpse.R
import org.lineageos.glimpse.models.Media
import org.lineageos.glimpse.models.MediaType

class ThumbnailAdapter(
    private val onItemSelected: (media: Media, position: Int) -> Unit,
) : BaseCursorAdapter<ThumbnailAdapter.ThumbnailViewHolder>() {
    private var recyclerView: RecyclerView? = null

    // Cursor indexes
    private var idIndex = -1
    private var isFavoriteIndex = -1
    private var isTrashedIndex = -1
    private var mediaTypeIndex = -1
    private var dateAddedIndex = -1

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = getIdFromMediaStore(position)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

        this.recyclerView = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ThumbnailViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.thumbnail_view, parent, false),
        onItemSelected
    )

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        getMediaFromMediaStore(position)?.let {
            holder.bind(it, position)
        }
    }

    override fun onChangedCursor(cursor: Cursor?) {
        super.onChangedCursor(cursor)

        cursor?.let {
            idIndex = it.getColumnIndex(MediaStore.Files.FileColumns._ID)
            isFavoriteIndex = it.getColumnIndex(MediaStore.Files.FileColumns.IS_FAVORITE)
            isTrashedIndex = it.getColumnIndex(MediaStore.Files.FileColumns.IS_TRASHED)
            mediaTypeIndex = it.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)
            dateAddedIndex = it.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)
        }
    }

    private fun getIdFromMediaStore(position: Int): Long {
        val cursor = cursor ?: return 0
        cursor.moveToPosition(position)
        return cursor.getLong(idIndex)
    }

    private fun getMediaFromMediaStore(position: Int): Media? {
        val cursor = cursor ?: return null

        cursor.moveToPosition(position)

        val id = cursor.getLong(idIndex)
        val isFavorite = cursor.getInt(isFavoriteIndex)
        val isTrashed = cursor.getInt(isTrashedIndex)
        val mediaType = cursor.getInt(mediaTypeIndex)
        val dateAdded = cursor.getLong(dateAddedIndex)

        return Media.fromMediaStore(
            id,
            isFavorite,
            isTrashed,
            mediaType,
            dateAdded,
        )
    }

    class ThumbnailViewHolder(
        view: View,
        private val onItemSelected: (media: Media, position: Int) -> Unit,
    ) : RecyclerView.ViewHolder(view) {
        // Views
        private val videoOverlayImageView =
            view.findViewById<ImageView>(R.id.videoOverlayImageView)!!
        private val thumbnailImageView = view.findViewById<ImageView>(R.id.thumbnailImageView)!!

        private lateinit var media: Media
        private var position = -1

        init {
            view.setOnClickListener {
                onItemSelected(media, position)
            }
        }

        fun bind(media: Media, position: Int) {
            this.media = media
            this.position = position

            thumbnailImageView.load(media.externalContentUri) {
                placeholder(R.drawable.thumbnail_placeholder)
            }
            videoOverlayImageView.isVisible = media.mediaType == MediaType.VIDEO
        }
    }
}
