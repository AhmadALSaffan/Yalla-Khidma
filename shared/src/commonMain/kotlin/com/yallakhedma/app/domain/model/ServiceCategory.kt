package com.yallakhedma.app.domain.model

import kotlinx.serialization.Serializable

/**
 * A service category managed in Firestore (`categories` collection). Loaded
 * dynamically so categories can be added/removed from the console without an
 * app update. `imagePath` is a Firebase Storage path (e.g. "categories/x.png").
 */
@Serializable
data class ServiceCategory(
    val id: String = "",
    val nameAr: String = "",
    val nameEn: String = "",
    val imagePath: String = "",
    val order: Int = 0,
)
