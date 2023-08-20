/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.search.SearchBar
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.lineageos.glimpse.R
import org.lineageos.glimpse.ext.getViewProperty
import org.lineageos.glimpse.recyclerview.LocationsCarouselAdapter
import org.lineageos.glimpse.ui.ListItem
import org.lineageos.glimpse.utils.PermissionsGatedCallback
import org.lineageos.glimpse.viewmodels.LocationViewModel

/**
 * A fragment showing a search bar with locations and categories.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment(R.layout.fragment_search) {
    // View models
    private val locationViewModel: LocationViewModel by viewModels()

    // Views
    private val appBarLayout by getViewProperty<AppBarLayout>(R.id.appBarLayout)
    private val categoriesLinearLayout by getViewProperty<LinearLayout>(R.id.categoriesLinearLayout)
    private val locationsCarouselRecyclerView by getViewProperty<RecyclerView>(R.id.locationsCarouselRecyclerView)
    private val searchBar by getViewProperty<SearchBar>(R.id.searchBar)
    private val viewAllLocationsButton by getViewProperty<Button>(R.id.viewAllLocationsButton)

    // Fragments
    private val parentNavController by lazy {
        requireParentFragment().requireParentFragment().findNavController()
    }

    // Coroutines
    private val mainScope = CoroutineScope(Job() + Dispatchers.Main)

    // MediaStore
    private val locationsCarouselAdapter by lazy {
        LocationsCarouselAdapter()
    }

    // Permissions
    private val permissionsGatedCallback = PermissionsGatedCallback(this) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mainScope.launch {
                    locationsCarouselAdapter.clearLocations()
                }
                locationViewModel.locations.take(10).collect {
                    mainScope.launch {
                        it?.also { location ->
                            locationsCarouselAdapter.addLocation(location)
                        } ?: locationsCarouselAdapter.clearLocations()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        appBarLayout.statusBarForeground = MaterialShapeDrawable.createWithElevationOverlay(context)

        locationsCarouselRecyclerView.adapter = locationsCarouselAdapter

        viewAllLocationsButton.setOnClickListener {
            parentNavController.navigate(R.id.action_mainFragment_to_locationsFragment)
        }

        categoriesLinearLayout.let {
            for (item in mapOf(
                R.string.search_category_photos to R.drawable.ic_photo_size_select_actual,
                R.string.search_category_videos to R.drawable.ic_play_circle_outline,
                R.string.search_category_favorites to R.drawable.ic_star_border,
                R.string.search_category_trashed to R.drawable.ic_delete,
            )) {
                it.addView(
                    ListItem(context).also { listItem ->
                        listItem.headlineText = resources.getString(item.key)
                        listItem.leadingIconImage = ResourcesCompat.getDrawable(
                            resources, item.value, null
                        )
                    }
                )
            }
        }

        permissionsGatedCallback.runAfterPermissionsCheck()
    }

    companion object {
        private fun createBundle() = bundleOf()

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment SearchFragment.
         */
        fun newInstance() = SearchFragment().apply {
            arguments = createBundle()
        }
    }
}
