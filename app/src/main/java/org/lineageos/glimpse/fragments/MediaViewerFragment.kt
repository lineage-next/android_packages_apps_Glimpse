/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.fragments

import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.shape.MaterialShapeDrawable
import org.lineageos.glimpse.R
import org.lineageos.glimpse.ext.KEY_ALBUM
import org.lineageos.glimpse.ext.KEY_POSITION
import org.lineageos.glimpse.ext.getParcelable
import org.lineageos.glimpse.ext.getViewProperty
import org.lineageos.glimpse.models.Album
import org.lineageos.glimpse.thumbnail.MediaViewerAdapter
import org.lineageos.glimpse.utils.MediaStoreRequests

class MediaViewerFragment : Fragment(R.layout.fragment_media_viewer),
    LoaderManager.LoaderCallbacks<Cursor> {
    // Views
    private val appBarLayout by getViewProperty<AppBarLayout>(R.id.appBarLayout)
    private val toolbar by getViewProperty<MaterialToolbar>(R.id.toolbar)
    private val infiniteViewPager by getViewProperty<ViewPager2>(R.id.viewPager)

    // ExoPlayer
    private val exoPlayer by lazy {
        ExoPlayer.Builder(requireContext()).build().apply {
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
        }
    }

    // Adapter
    private val mediaViewerAdapter by lazy {
        MediaViewerAdapter(exoPlayer)
    }

    // MediaStore
    private val loaderManagerInstance by lazy { LoaderManager.getInstance(this) }

    // Arguments
    private val position by lazy { arguments?.getInt(KEY_POSITION, -1)!! }
    private val album by lazy { arguments?.getParcelable(KEY_ALBUM, Album::class) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            infiniteViewPager.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin = insets.right
            }
            infiniteViewPager.updatePadding(bottom = insets.bottom)

            windowInsets
        }

        infiniteViewPager.adapter = mediaViewerAdapter

        initCursorLoader()
    }

    override fun onDestroyView() {
        exoPlayer.stop()
        exoPlayer.release()
        super.onDestroyView()
    }

    override fun onCreateLoader(id: Int, args: Bundle?) = when (id) {
        MediaStoreRequests.MEDIA_STORE_REELS_LOADER_ID.ordinal -> {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
            )
            val selection = buildString {
                append("(")
                append(buildString {
                    append(MediaStore.Files.FileColumns.MEDIA_TYPE)
                    append("=")
                    append(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
                    append(" OR ")
                    append(MediaStore.Files.FileColumns.MEDIA_TYPE)
                    append("=")
                    append(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
                })
                append(")")
                album?.let {
                    append(
                        buildString {
                            append(" AND ")
                            append(MediaStore.Files.FileColumns.BUCKET_ID)
                            append(" = ?")
                        }
                    )
                }
            }
            CursorLoader(
                requireContext(),
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                album?.let { arrayOf(it.id.toString()) },
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
            )
        }

        else -> throw Exception("Unknown ID $id")
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mediaViewerAdapter.changeCursor(null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        mediaViewerAdapter.changeCursor(data)
        infiniteViewPager.setCurrentItem(position, false)
    }

    private fun initCursorLoader() {
        loaderManagerInstance.initLoader(
            MediaStoreRequests.MEDIA_STORE_REELS_LOADER_ID.ordinal, null, this
        )
    }
}