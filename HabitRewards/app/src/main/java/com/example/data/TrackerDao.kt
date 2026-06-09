package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Update
    suspend fun updateUserProfile(profile: UserProfile)

    // Challenges
    @Query("SELECT * FROM challenges ORDER BY id DESC")
    fun getAllChallenges(): Flow<List<Challenge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: Challenge): Long

    @Query("DELETE FROM challenges WHERE id = :id")
    suspend fun deleteChallengeById(id: Int)

    @Query("SELECT * FROM challenges WHERE id = :id LIMIT 1")
    suspend fun getChallengeById(id: Int): Challenge?

    // Challenge Days
    @Query("SELECT * FROM challenge_days WHERE challengeId = :challengeId ORDER BY dayIndex ASC")
    fun getDaysForChallenge(challengeId: Int): Flow<List<ChallengeDay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallengeDay(day: ChallengeDay)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallengeDays(days: List<ChallengeDay>)

    @Query("DELETE FROM challenge_days WHERE challengeId = :challengeId")
    suspend fun deleteChallengeDays(challengeId: Int)

    // Wishlist / Vision board
    @Query("SELECT * FROM wishlist_items ORDER BY id DESC")
    fun getAllWishlistItems(): Flow<List<WishlistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: WishlistItem)

    @Query("DELETE FROM wishlist_items WHERE id = :id")
    suspend fun deleteWishlistItemById(id: Int)

    // Affiliate Store / Monetization
    @Query("SELECT * FROM affiliate_offers ORDER BY id ASC")
    fun getAllAffiliateOffers(): Flow<List<AffiliateOffer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAffiliateOffers(offers: List<AffiliateOffer>)

    @Update
    suspend fun updateAffiliateOffer(offer: AffiliateOffer)

    @Query("DELETE FROM affiliate_offers")
    suspend fun clearAffiliateOffers()
}
