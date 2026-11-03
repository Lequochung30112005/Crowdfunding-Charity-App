package com.example.charityapp.models

import com.google.firebase.Timestamp

data class Donation(
    val id: String = "",
    val campaignId: String = "",
    val campaignTitle: String = "",
    val userName: String = "",
    val amount: Double = 0.0,
    val timestamp: Timestamp? = null
)