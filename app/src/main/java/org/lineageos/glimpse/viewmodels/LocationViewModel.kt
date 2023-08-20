/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.glimpse.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.lineageos.glimpse.repository.MediaRepository

class LocationViewModel(
    application: Application,
) : GlimpseViewModel(application) {
    val locations = MediaRepository.locations(context).stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = null,
    )
}
