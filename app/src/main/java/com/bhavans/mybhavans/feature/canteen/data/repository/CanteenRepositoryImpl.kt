package com.bhavans.mybhavans.feature.canteen.data.repository

import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.canteen.domain.model.Canteen
import com.bhavans.mybhavans.feature.canteen.domain.model.CheckIn
import com.bhavans.mybhavans.feature.canteen.domain.model.CrowdLevel
import com.bhavans.mybhavans.feature.canteen.domain.repository.CanteenRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CanteenRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CanteenRepository {

    private val canteensCollection = firestore.collection("canteens")
    private val checkInsCollection = firestore.collection("checkins")

    override fun getCanteens(): Flow<Resource<List<Canteen>>> = callbackFlow {
        trySend(Resource.Loading())
        
        val listener = canteensCollection
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load canteens"))
                    return@addSnapshotListener
                }
                
                val canteens = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CanteenDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                
                trySend(Resource.Success(canteens))
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun getCanteen(canteenId: String): Resource<Canteen> {
        return try {
            val doc = canteensCollection.document(canteenId).get().await()
            val canteen = doc.toObject(CanteenDto::class.java)?.toDomain(doc.id)
            if (canteen != null) {
                Resource.Success(canteen)
            } else {
                Resource.Error("Canteen not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get canteen")
        }
    }

    override fun getRecentCheckIns(canteenId: String, limit: Int): Flow<Resource<List<CheckIn>>> = callbackFlow {
        trySend(Resource.Loading())
        
        val listener = checkInsCollection
            .whereEqualTo("canteenId", canteenId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load check-ins"))
                    return@addSnapshotListener
                }
                
                val checkIns = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CheckInDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                
                trySend(Resource.Success(checkIns))
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun checkIn(
        canteenId: String,
        crowdLevel: CrowdLevel,
        waitTime: Int,
        comment: String
    ): Resource<CheckIn> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("User not authenticated")
            
            val checkInDto = CheckInDto(
                canteenId = canteenId,
                userId = currentUser.uid,
                userName = currentUser.displayName ?: "Anonymous",
                crowdLevel = crowdLevel.name,
                waitTime = waitTime,
                comment = comment,
                createdAt = System.currentTimeMillis()
            )
            
            val docRef = checkInsCollection.add(checkInDto).await()
            val checkIn = checkInDto.toDomain(docRef.id)
            
            // Update canteen crowd level after check-in
            updateCanteenCrowdLevel(canteenId)
            
            Resource.Success(checkIn)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to check in")
        }
    }

    override suspend fun updateCanteenCrowdLevel(canteenId: String): Resource<Unit> {
        return try {
            // Get recent check-ins from last 30 minutes
            val thirtyMinutesAgo = System.currentTimeMillis() - (30 * 60 * 1000)
            val recentCheckIns = checkInsCollection
                .whereEqualTo("canteenId", canteenId)
                .whereGreaterThan("createdAt", thirtyMinutesAgo)
                .get()
                .await()
            
            val checkInCount = recentCheckIns.documents.size
            val avgCrowdLevel = if (recentCheckIns.documents.isNotEmpty()) {
                val levels = recentCheckIns.documents.mapNotNull { doc ->
                    doc.toObject(CheckInDto::class.java)?.crowdLevel
                }
                calculateAverageCrowdLevel(levels)
            } else {
                CrowdLevel.MODERATE
            }
            
            val avgWaitTime = if (recentCheckIns.documents.isNotEmpty()) {
                val waitTimes = recentCheckIns.documents.mapNotNull { doc ->
                    doc.toObject(CheckInDto::class.java)?.waitTime
                }
                waitTimes.average().toInt()
            } else {
                10
            }
            
            val crowdPercentage = when (avgCrowdLevel) {
                CrowdLevel.EMPTY -> 10
                CrowdLevel.LOW -> 30
                CrowdLevel.MODERATE -> 50
                CrowdLevel.BUSY -> 70
                CrowdLevel.CROWDED -> 90
            }
            
            canteensCollection.document(canteenId).update(
                mapOf(
                    "currentCrowdLevel" to avgCrowdLevel.name,
                    "crowdPercentage" to crowdPercentage,
                    "checkInsLast30Min" to checkInCount,
                    "avgWaitTime" to avgWaitTime,
                    "lastUpdated" to System.currentTimeMillis()
                )
            ).await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update crowd level")
        }
    }
    
    private fun calculateAverageCrowdLevel(levels: List<String>): CrowdLevel {
        if (levels.isEmpty()) return CrowdLevel.MODERATE
        
        val levelValues = levels.map { levelName ->
            try {
                CrowdLevel.valueOf(levelName).ordinal
            } catch (e: Exception) {
                CrowdLevel.MODERATE.ordinal
            }
        }
        
        val avg = levelValues.average().toInt()
        return CrowdLevel.entries.getOrElse(avg) { CrowdLevel.MODERATE }
    }
}

// DTO classes for Firebase serialization
data class CanteenDto(
    val name: String = "",
    val location: String = "",
    val imageUrl: String? = null,
    val currentCrowdLevel: String = "MODERATE",
    val crowdPercentage: Int = 50,
    val checkInsLast30Min: Int = 0,
    val isOpen: Boolean = true,
    val openTime: String = "08:00",
    val closeTime: String = "18:00",
    val specialItems: List<String> = emptyList(),
    val avgWaitTime: Int = 10,
    val lastUpdated: Long = 0
) {
    fun toDomain(id: String): Canteen {
        return Canteen(
            id = id,
            name = name,
            location = location,
            imageUrl = imageUrl,
            currentCrowdLevel = try { CrowdLevel.valueOf(currentCrowdLevel) } catch (e: Exception) { CrowdLevel.MODERATE },
            crowdPercentage = crowdPercentage,
            checkInsLast30Min = checkInsLast30Min,
            isOpen = isOpen,
            openTime = openTime,
            closeTime = closeTime,
            specialItems = specialItems,
            avgWaitTime = avgWaitTime,
            lastUpdated = lastUpdated
        )
    }
}

data class CheckInDto(
    val canteenId: String = "",
    val userId: String = "",
    val userName: String = "",
    val crowdLevel: String = "MODERATE",
    val waitTime: Int = 10,
    val comment: String = "",
    val createdAt: Long = 0
) {
    fun toDomain(id: String): CheckIn {
        return CheckIn(
            id = id,
            canteenId = canteenId,
            userId = userId,
            userName = userName,
            crowdLevel = try { CrowdLevel.valueOf(crowdLevel) } catch (e: Exception) { CrowdLevel.MODERATE },
            waitTime = waitTime,
            comment = comment,
            createdAt = createdAt
        )
    }
}
