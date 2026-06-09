package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class TrackerRepository(private val context: Context, private val dao: TrackerDao) {

    private val httpClient = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val allChallenges: Flow<List<Challenge>> = dao.getAllChallenges()
    val wishlistItems: Flow<List<WishlistItem>> = dao.getAllWishlistItems()
    val allOffers: Flow<List<AffiliateOffer>> = dao.getAllAffiliateOffers()

    fun getDaysForChallenge(challengeId: Int): Flow<List<ChallengeDay>> {
        return dao.getDaysForChallenge(challengeId)
    }

    // Onboarding / Profile setup
    suspend fun saveProfile(username: String, profilePicBase64: String?, age: String?, email: String?) {
        withContext(Dispatchers.IO) {
            val current = dao.getUserProfileSync()
            val newProfile = UserProfile(
                id = 1,
                username = username,
                profilePicBase64 = profilePicBase64 ?: current?.profilePicBase64,
                age = age ?: current?.age,
                email = email ?: current?.email,
                tokenBalance = current?.tokenBalance ?: 150 // Free starting tokens so they can quickly test coupon redemptions
            )
            dao.insertUserProfile(newProfile)
            syncWithFirebase(newProfile)
        }
    }

    // Token Economy Add
    suspend fun addTokens(amount: Int) {
        withContext(Dispatchers.IO) {
            val current = dao.getUserProfileSync()
            if (current != null) {
                val updated = current.copy(tokenBalance = current.tokenBalance + amount)
                dao.insertUserProfile(updated)
                syncWithFirebase(updated)
            }
        }
    }

    // Token Economy Deduct
    suspend fun deductTokens(amount: Int): Boolean {
        return withContext(Dispatchers.IO) {
            val current = dao.getUserProfileSync()
            if (current != null && current.tokenBalance >= amount) {
                val updated = current.copy(tokenBalance = current.tokenBalance - amount)
                dao.insertUserProfile(updated)
                syncWithFirebase(updated)
                true
            } else {
                false
            }
        }
    }

    // Creating Challenge: 최소 기간 2일 준수!
    suspend fun createChallenge(name: String, duration: Int, colorHex: String): Long {
        return withContext(Dispatchers.IO) {
            val validatedDuration = if (duration < 2) 2 else duration
            val challenge = Challenge(
                name = name,
                durationDays = validatedDuration,
                baseThemeColorHex = colorHex
            )
            val challengeId = dao.insertChallenge(challenge).toInt()

            // Automatically pre-populate default day objects for the dynamic calendar grid
            val daysList = (0 until validatedDuration).map { index ->
                ChallengeDay(
                    key = "${challengeId}_$index",
                    challengeId = challengeId,
                    dayIndex = index,
                    dayColorHex = colorHex
                )
            }
            dao.insertChallengeDays(daysList)
            challengeId.toLong()
        }
    }

    // Day Logging
    suspend fun logDay(day: ChallengeDay) {
        withContext(Dispatchers.IO) {
            dao.insertChallengeDay(day)
            // If logging awards tokens (only once for success entries), increment balance
            if (day.isLogged) {
                // Award 10 extra tokens for adding local camera/gallery photos, 5 tokens for text/emoji-only log!
                val awardAmount = if (!day.stickerBase64.isNullOrEmpty()) 15 else 5
                addTokens(awardAmount)
            }
        }
    }

    suspend fun deleteChallenge(challengeId: Int) {
        withContext(Dispatchers.IO) {
            dao.deleteChallengeById(challengeId)
            dao.deleteChallengeDays(challengeId)
        }
    }

    // Goals List
    suspend fun addGoal(title: String, desc: String) {
        withContext(Dispatchers.IO) {
            dao.insertWishlistItem(
                WishlistItem(
                    title = title,
                    description = desc
                )
            )
        }
    }

    suspend fun deleteGoal(id: Int) {
        withContext(Dispatchers.IO) {
            dao.deleteWishlistItemById(id)
        }
    }

    // Store Coupon Redeem
    suspend fun redeemCoupon(offerId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val offers = dao.getAllAffiliateOffers().firstOrNull() ?: emptyList()
            val targetOffer = offers.find { it.id == offerId }
            if (targetOffer != null && !targetOffer.isUnlocked) {
                val success = deductTokens(targetOffer.tokenCost)
                if (success) {
                    val updatedOffer = targetOffer.copy(isUnlocked = true)
                    dao.updateAffiliateOffer(updatedOffer)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }

    // Populate Store templates
    suspend fun ensureAffiliateOffersPopulated() {
        withContext(Dispatchers.IO) {
            val currentOffers = dao.getAllAffiliateOffers().firstOrNull() ?: emptyList()
            if (currentOffers.isEmpty()) {
                val templates = listOf(
                    AffiliateOffer(
                        id = "prod_1",
                        title = "Ergonomic Memory Cushion",
                        description = "Optimize task pacing and posture. High-quality orthosis support with skin-safe covers.",
                        imageUrl = "https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?w=150", 
                        affiliateUrl = "https://www.aliexpress.com",
                        couponCode = "HABITCORE45",
                        tokenCost = 100,
                        isUnlocked = false
                    ),
                    AffiliateOffer(
                        id = "prod_2",
                        title = "Smart Circular Focus Lamp",
                        description = "Simulate natural daylight thresholds to trigger visual flow states dynamically.",
                        imageUrl = "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=150",
                        affiliateUrl = "https://www.amazon.com",
                        couponCode = "LIGHTFOCUS50",
                        tokenCost = 100,
                        isUnlocked = false
                    ),
                    AffiliateOffer(
                        id = "prod_3",
                        title = "Minimalist Stainless Water Bottle",
                        description = "Smart double-wall vacuum flask keeping vital hydration close at hand during long schedules.",
                        imageUrl = "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=150",
                        affiliateUrl = "https://www.aliexpress.com",
                        couponCode = "HYDRATEME80",
                        tokenCost = 100,
                        isUnlocked = false
                    )
                )
                dao.insertAffiliateOffers(templates)
            }
        }
    }

    // Optional Firebase integration syncing user tokens over HTTP REST
    fun getFirebaseRestUrl(): String? {
        val prefs = context.getSharedPreferences("firebase_prefs", Context.MODE_PRIVATE)
        return prefs.getString("rest_endpoints", null)
    }

    fun saveFirebaseRestUrl(url: String) {
        val prefs = context.getSharedPreferences("firebase_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("rest_endpoints", url).apply()
    }

    suspend fun syncWithFirebase(profile: UserProfile? = null) {
        val url = getFirebaseRestUrl() ?: return
        val activeProfile = profile ?: dao.getUserProfileSync() ?: return

        withContext(Dispatchers.IO) {
            try {
                // Realtime DB compatible PUT: pushes profile balance and metadata automatically
                val syncPayload = JSONObject().apply {
                    put("username", activeProfile.username)
                    put("tokenBalance", activeProfile.tokenBalance)
                    put("age", activeProfile.age ?: "")
                    put("email", activeProfile.email ?: "")
                    put("lastSynced", System.currentTimeMillis())
                }.toString()

                val endpoint = if (url.endsWith(".json")) url else "${url.trimEnd('/')}/users/${activeProfile.username}.json"

                val request = Request.Builder()
                    .url(endpoint)
                    .put(syncPayload.toRequestBody(jsonMediaType))
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("FirebaseSync", "API push failed: ${response.code}")
                    } else {
                        Log.i("FirebaseSync", "User tokens updated securely in real Firebase console.")
                    }
                }
            } catch (e: Exception) {
                Log.e("FirebaseSync", "No physical Firebase route or missing config network: ${e.message}")
            }
        }
    }
}
