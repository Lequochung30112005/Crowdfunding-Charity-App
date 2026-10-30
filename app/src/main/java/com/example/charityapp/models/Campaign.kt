package com.example.charityapp.models

data class Campaign(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val imageUrl: String = ""
)
