package fr.harmony.userImages

import fr.harmony.userImages.data.SharedImage

data class StateUserImages (
    val images: List<SharedImage> = emptyList(),

    val popupImage: SharedImage? = null,
    val popupVisible: Boolean = false,
    val error: String? = null
)