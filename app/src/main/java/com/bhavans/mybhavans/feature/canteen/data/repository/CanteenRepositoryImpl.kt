package com.bhavans.mybhavans.feature.canteen.data.repository

import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.canteen.domain.model.Canteen
import com.bhavans.mybhavans.feature.canteen.domain.model.CheckIn
import com.bhavans.mybhavans.feature.canteen.domain.model.CrowdLevel
import com.bhavans.mybhavans.feature.canteen.domain.model.MenuItem
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
                
                if (canteens.isEmpty()) {
                    // Seed default canteen if none exist
                    seedDefaultCanteen()
                }
                
                trySend(Resource.Success(canteens))
            }
        
        awaitClose { listener.remove() }
    }

    private fun seedDefaultCanteen() {
        val defaultMenuItems = listOf(
            mapOf("name" to "Vada Pav", "price" to 20.0, "category" to "Snacks", "isAvailable" to true),
            mapOf("name" to "Samosa", "price" to 15.0, "category" to "Snacks", "isAvailable" to true),
            mapOf("name" to "Tea (Chai)", "price" to 10.0, "category" to "Beverages", "isAvailable" to true),
            mapOf("name" to "Coffee", "price" to 15.0, "category" to "Beverages", "isAvailable" to true),
            mapOf("name" to "Cold Coffee", "price" to 30.0, "category" to "Beverages", "isAvailable" to true),
            mapOf("name" to "Thali (Veg)", "price" to 70.0, "category" to "Meals", "isAvailable" to true),
            mapOf("name" to "Pav Bhaji", "price" to 50.0, "category" to "Meals", "isAvailable" to true),
            mapOf("name" to "Misal Pav", "price" to 45.0, "category" to "Meals", "isAvailable" to true),
            mapOf("name" to "Sandwich", "price" to 35.0, "category" to "Snacks", "isAvailable" to true),
            mapOf("name" to "Maggi", "price" to 25.0, "category" to "Snacks", "isAvailable" to true),
            mapOf("name" to "Biryani", "price" to 80.0, "category" to "Meals", "isAvailable" to true),
            mapOf("name" to "Frankie", "price" to 40.0, "category" to "Snacks", "isAvailable" to true),
            mapOf("name" to "Manchurian", "price" to 50.0, "category" to "Chinese", "isAvailable" to true),
            mapOf("name" to "Fried Rice", "price" to 55.0, "category" to "Chinese", "isAvailable" to true),
            mapOf("name" to "Juice (Fresh)", "price" to 25.0, "category" to "Beverages", "isAvailable" to true),
            mapOf("name" to "Buttermilk", "price" to 10.0, "category" to "Beverages", "isAvailable" to true)
        )

        val canteenData = hashMapOf(
            "name" to "Bhavans Canteen",
            "location" to "Ground Floor, Main Building",
            "currentCrowdLevel" to "MODERATE",
            "crowdPercentage" to 50,
            "checkInsLast30Min" to 0,
            "isOpen" to true,
            "openTime" to "08:00",
            "closeTime" to "17:00",
            "specialItems" to listOf("Pav Bhaji", "Cold Coffee", "Biryani"),
            "avgWaitTime" to 10,
            "menuItems" to defaultMenuItems,
            "totalSeats" to 120,
            "occupiedSeats" to 0,
            "lastUpdated" to System.currentTimeMillis()
        )

        canteensCollection.document("bhavans_main").set(canteenData)
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
    val lastUpdated: Long = 0,
    val menuItems: List<Map<String, Any>> = emptyList(),
    val totalSeats: Int = 100,
    val occupiedSeats: Int = 0
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
            lastUpdated = lastUpdated,
            menuItems = menuItems.map { m ->
                MenuItem(
                    name = m["name"] as? String ?: "",
                    price = (m["price"] as? Number)?.toDouble() ?: 0.0,
                    category = m["category"] as? String ?: "General",
                    isAvailable = m["isAvailable"] as? Boolean ?: true
                )
            },
            totalSeats = totalSeats,
            occupiedSeats = occupiedSeats
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
