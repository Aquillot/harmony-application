package fr.harmony.explore

import fr.harmony.explore.data.SharedImage

data class StateExplore (
    val images: List<SharedImage> = emptyList(),
    val sliderImage: SharedImage? = null,

    val sliderVisible: Boolean = false,
    val error: String? = null
)