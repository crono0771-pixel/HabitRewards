package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Only 1 active user profile
    val username: String,
    val profilePicBase64: String? = null,
    val age: String? = null,
    val email: String? = null,
    val tokenBalance: Int = 150 // Free starting token balance (150 so they can immediately test 100-token coupon unlock!)
)

@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val durationDays: Int, // Minimum allowed is 2 days
    val startDate: Long = System.currentTimeMillis(),
    val baseThemeColorHex: String = "#3B82F6" // Default modern primary color
)

@Entity(tableName = "challenge_days")
data class ChallengeDay(
    @PrimaryKey val key: String, // format: "${challengeId}_${dayIndex}" for unique rows
    val challengeId: Int,
    val dayIndex: Int,
    val moodEmoji: String? = null,
    val dayColorHex: String? = null,
    val stickerBase64: String? = null, // Programmatically cropped local bitmap stored as Base64 string
    val isLogged: Boolean = false,
    val loggedTimestamp: Long = 0
)

@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "affiliate_offers")
data class AffiliateOffer(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String, // Simulated or real product image URL
    val affiliateUrl: String, // Aliexpress / Amazon redirect link
    val couponCode: String,
    val tokenCost: Int = 100,
    val isUnlocked: Boolean = false
)
