package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TrackerRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TrackerRepository(application, database.trackerDao())
        
        // Ensure standard starter storefront listings are loaded cleanly
        viewModelScope.launch {
            repository.ensureAffiliateOffersPopulated()
        }
    }

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allChallenges: StateFlow<List<Challenge>> = repository.allChallenges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistItems: StateFlow<List<WishlistItem>> = repository.wishlistItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOffers: StateFlow<List<AffiliateOffer>> = repository.allOffers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getDaysForChallenge(challengeId: Int): Flow<List<ChallengeDay>> {
        return repository.getDaysForChallenge(challengeId)
    }

    // Profile Onboarding action
    fun createProfile(username: String, profilePicBase64: String?, age: String?, email: String?) {
        viewModelScope.launch {
            repository.saveProfile(username, profilePicBase64, age, email)
        }
    }

    // Create custom multi-day micro-habit challenge
    fun addChallenge(name: String, duration: Int, themeColorHex: String) {
        viewModelScope.launch {
            repository.createChallenge(name, duration, themeColorHex)
        }
    }

    fun removeChallenge(challengeId: Int) {
        viewModelScope.launch {
            repository.deleteChallenge(challengeId)
        }
    }

    // Log individual challenge day status
    fun logChallengeDay(day: ChallengeDay) {
        viewModelScope.launch {
            repository.logDay(day)
        }
    }

    // Goals / Vision Board
    fun addGoal(title: String, desc: String) {
        viewModelScope.launch {
            repository.addGoal(title, desc)
        }
    }

    fun removeGoal(id: Int) {
        viewModelScope.launch {
            repository.deleteGoal(id)
        }
    }

    // Affiliate coupon unlock with user token validation and real-time syncing
    private val _redeemStatus = MutableSharedFlow<Pair<Boolean, String>>()
    val redeemStatus = _redeemStatus.asSharedFlow()

    fun attemptRedeem(offerId: String) {
        viewModelScope.launch {
            val success = repository.redeemCoupon(offerId)
            _redeemStatus.emit(success to offerId)
        }
    }

    // Optional Firebase integration url
    fun loadFirebaseConfigUrl(): String? {
        return repository.getFirebaseRestUrl()
    }

    fun updateFirebaseConfigUrl(url: String) {
        repository.saveFirebaseRestUrl(url)
        viewModelScope.launch {
            repository.syncWithFirebase()
        }
    }
}
