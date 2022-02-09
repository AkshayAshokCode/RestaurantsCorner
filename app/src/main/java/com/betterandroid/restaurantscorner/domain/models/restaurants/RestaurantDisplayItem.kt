package com.betterandroid.restaurantscorner.domain.models.restaurants

data class RestaurantDisplayItem(
    val id: Int,
    val displayName: String,
    val displayDistance: String,
    val imageUrl: String,
    var type: RestaurantType
)