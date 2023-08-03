/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.media3.common.C.TIME_UNSET
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.fragment.findNavController
import coil.load
import org.lineageos.glimpse.R
import org.lineageos.glimpse.ext.*
import org.lineageos.glimpse.models.Album
import org.lineageos.glimpse.models.Media
import org.lineageos.glimpse.models.MediaType
import org.lineageos.glimpse.utils.CommonNavigationArguments
import java.text.SimpleDateFormat

/**
 * A fragment showing a media that supports scrolling before and after it.
 * Use the [MediaViewerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MediaViewerFragment : Fragment(R.layout.fragment_media_viewer) {
    // Views
    private val adjustButton by getViewProperty<ImageButton>(R.id.adjustButton)
    private val backButton by getViewProperty<ImageButton>(R.id.backButton)
    private val bottomSheetLinearLayout by getViewProperty<LinearLayout>(R.id.bottomSheetLinearLayout)
    private val dateTextView by getViewProperty<TextView>(R.id.dateTextView)
    private val deleteButton by getViewProperty<ImageButton>(R.id.deleteButton)
    private val favoriteButton by getViewProperty<ImageButton>(R.id.favoriteButton)
    private val imageView by getViewProperty<ImageView>(R.id.imageView)
    private val playerView by getViewProperty<PlayerView>(R.id.playerView)
    private val shareButton by getViewProperty<ImageButton>(R.id.shareButton)
    private val timeTextView by getViewProperty<TextView>(R.id.timeTextView)
    private val topSheetConstraintLayout by getViewProperty<ConstraintLayout>(R.id.topSheetConstraintLayout)

    // ExoPlayer
    private val exoPlayer by lazy {
        ExoPlayer.Builder(requireContext()).build().also {
            playerView.player = it
        }.apply {
            playWhenReady = true
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
        }
    }

    private var album: Album? = null
    private lateinit var media: Media

    private var position: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            position = it.getInt(KEY_POSITION, -1)
            album = it.getParcelable(KEY_ALBUM, Album::class)
            media = it.getParcelable(KEY_MEDIA, Media::class)!!
        }

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            topSheetConstraintLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin = insets.right
                topMargin = insets.top
            }
            bottomSheetLinearLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom
                leftMargin = insets.left
                rightMargin = insets.right
            }

            windowInsets
        }

        loadMedia()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        playerView.player = null
        exoPlayer.stop()
        exoPlayer.release()
    }

    private fun loadMedia() {
        when (media.mediaType) {
            MediaType.IMAGE -> {
                imageView.load(media.externalContentUri)
            }

            MediaType.VIDEO -> {
                with(exoPlayer) {
                    setMediaItem(MediaItem.fromUri(media.externalContentUri))
                    seekTo(TIME_UNSET)
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
        private const val KEY_ALBUM = "album"
        private const val KEY_MEDIA = "media"
        private const val KEY_POSITION = "position"

        private val dateFormatter = SimpleDateFormat.getDateInstance()
        private val timeFormatter = SimpleDateFormat.getTimeInstance()

        fun createBundle(
            album: Album?,
            media: Media,
            position: Int,
        ) = CommonNavigationArguments().toBundle().apply {
            putParcelable(KEY_ALBUM, album)
            putParcelable(KEY_MEDIA, media)
            putInt(KEY_POSITION, position)
        }

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param album Album.
         * @return A new instance of fragment ReelsFragment.
         */
        fun newInstance(
            album: Album?,
            media: Media,
            position: Int,
        ) = MediaViewerFragment().apply {
            arguments = createBundle(
                album,
                media,
                position,
            )
        }
    }
}
