package com.bhavans.mybhavans.feature.skillswap.data.repository

import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.skillswap.domain.model.MatchStatus
import com.bhavans.mybhavans.feature.skillswap.domain.model.Skill
import com.bhavans.mybhavans.feature.skillswap.domain.model.SkillCategory
import com.bhavans.mybhavans.feature.skillswap.domain.model.SkillLevel
import com.bhavans.mybhavans.feature.skillswap.domain.model.SkillMatch
import com.bhavans.mybhavans.feature.skillswap.domain.repository.SkillSwapRepository
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
class SkillSwapRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : SkillSwapRepository {

    private val skillsCollection = firestore.collection("skills")
    private val matchesCollection = firestore.collection("skill_matches")

    override fun getSkills(
        category: SkillCategory?,
        isTeaching: Boolean?
    ): Flow<Resource<List<Skill>>> = callbackFlow {
        trySend(Resource.Loading())
        
        var query: Query = skillsCollection
            .whereEqualTo("isActive", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
        
        if (category != null) {
            query = query.whereEqualTo("category", category.name)
        }
        
        if (isTeaching != null) {
            query = query.whereEqualTo("isTeaching", isTeaching)
        }
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Failed to load skills"))
                return@addSnapshotListener
            }
            
            val skills = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(SkillDto::class.java)?.toDomain(doc.id)
            } ?: emptyList()
            
            trySend(Resource.Success(skills))
        }
        
        awaitClose { listener.remove() }
    }

    override suspend fun getSkill(skillId: String): Resource<Skill> {
        return try {
            val doc = skillsCollection.document(skillId).get().await()
            val skill = doc.toObject(SkillDto::class.java)?.toDomain(doc.id)
            if (skill != null) {
                Resource.Success(skill)
            } else {
                Resource.Error("Skill not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get skill")
        }
    }

    override fun getUserSkills(userId: String): Flow<Resource<List<Skill>>> = callbackFlow {
        trySend(Resource.Loading())
        
        val listener = skillsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load skills"))
                    return@addSnapshotListener
                }
                
                val skills = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SkillDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                
                trySend(Resource.Success(skills))
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun createSkill(
        title: String,
        description: String,
        category: SkillCategory,
        level: SkillLevel,
        isTeaching: Boolean,
        lookingFor: List<SkillCategory>,
        availability: String,
        contactPreference: String,
        phoneNumber: String?
    ): Resource<Skill> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("User not authenticated")
            
            val now = System.currentTimeMillis()
            val skillDto = SkillDto(
                userId = currentUser.uid,
                userName = currentUser.displayName ?: "Anonymous",
                userEmail = currentUser.email ?: "",
                userProfileUrl = currentUser.photoUrl?.toString(),
                title = title,
                description = description,
                category = category.name,
                level = level.name,
                isTeaching = isTeaching,
                lookingFor = lookingFor.map { it.name },
                availability = availability,
                contactPreference = contactPreference,
                phoneNumber = phoneNumber,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
            
            val docRef = skillsCollection.add(skillDto).await()
            val skill = skillDto.toDomain(docRef.id)
            
            Resource.Success(skill)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create skill")
        }
    }

    override suspend fun updateSkill(skill: Skill): Resource<Skill> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("User not authenticated")
            
            // Verify ownership
            val doc = skillsCollection.document(skill.id).get().await()
            val existingSkill = doc.toObject(SkillDto::class.java)
            if (existingSkill?.userId != currentUser.uid) {
                return Resource.Error("You can only edit your own skills")
            }
            
            val updatedDto = SkillDto(
                userId = skill.userId,
                userName = skill.userName,
                userEmail = skill.userEmail,
                userProfileUrl = skill.userProfileUrl,
                title = skill.title,
                description = skill.description,
                category = skill.category.name,
                level = skill.level.name,
                isTeaching = skill.isTeaching,
                lookingFor = skill.lookingFor.map { it.name },
                availability = skill.availability,
                contactPreference = skill.contactPreference,
                phoneNumber = skill.phoneNumber,
                isActive = skill.isActive,
                createdAt = skill.createdAt,
                updatedAt = System.currentTimeMillis()
            )
            
            skillsCollection.document(skill.id).set(updatedDto).await()
            Resource.Success(skill.copy(updatedAt = updatedDto.updatedAt))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update skill")
        }
    }

    override suspend fun deleteSkill(skillId: String): Resource<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("User not authenticated")
            
            val doc = skillsCollection.document(skillId).get().await()
            val skill = doc.toObject(SkillDto::class.java)
            if (skill?.userId != currentUser.uid) {
                return Resource.Error("You can only delete your own skills")
            }
            
            skillsCollection.document(skillId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete skill")
        }
    }

    override suspend fun toggleSkillActive(skillId: String, isActive: Boolean): Resource<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("User not authenticated")
            
            val doc = skillsCollection.document(skillId).get().await()
            val skill = doc.toObject(SkillDto::class.java)
            if (skill?.userId != currentUser.uid) {
                return Resource.Error("You can only modify your own skills")
            }
            
            skillsCollection.document(skillId).update("isActive", isActive).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update skill status")
        }
    }

    override suspend fun sendMatchRequest(skillId: String, message: String): Resource<SkillMatch> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("User not authenticated")
            
            // Get the skill details
            val skillDoc = skillsCollection.document(skillId).get().await()
            val skill = skillDoc.toObject(SkillDto::class.java) 
                ?: return Resource.Error("Skill not found")
            
            if (skill.userId == currentUser.uid) {
                return Resource.Error("You cannot request your own skill")
            }
            
            val matchDto = SkillMatchDto(
                requesterId = currentUser.uid,
                requesterName = currentUser.displayName ?: "Anonymous",
                providerId = skill.userId,
                providerName = skill.userName,
                skillId = skillId,
                skillTitle = skill.title,
                message = message,
                status = MatchStatus.PENDING.name,
                createdAt = System.currentTimeMillis()
            )
            
            val docRef = matchesCollection.add(matchDto).await()
            val match = matchDto.toDomain(docRef.id)
            
            Resource.Success(match)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send request")
        }
    }

    override fun getMyMatchRequests(): Flow<Resource<List<SkillMatch>>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(Resource.Error("User not authenticated"))
            close()
            return@callbackFlow
        }
        
        trySend(Resource.Loading())
        
        val listener = matchesCollection
            .whereEqualTo("requesterId", currentUser.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load requests"))
                    return@addSnapshotListener
                }
                
                val matches = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SkillMatchDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                
                trySend(Resource.Success(matches))
            }
        
        awaitClose { listener.remove() }
    }

    override fun getReceivedMatchRequests(): Flow<Resource<List<SkillMatch>>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(Resource.Error("User not authenticated"))
            close()
            return@callbackFlow
        }
        
        trySend(Resource.Loading())
        
        val listener = matchesCollection
            .whereEqualTo("providerId", currentUser.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load requests"))
                    return@addSnapshotListener
                }
                
                val matches = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SkillMatchDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                
                trySend(Resource.Success(matches))
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun updateMatchStatus(matchId: String, status: MatchStatus): Resource<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Resource.Error("User not authenticated")
            
            val doc = matchesCollection.document(matchId).get().await()
            val match = doc.toObject(SkillMatchDto::class.java)
            if (match?.providerId != currentUser.uid) {
                return Resource.Error("Only the skill provider can update match status")
            }
            
            matchesCollection.document(matchId).update("status", status.name).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update request status")
        }
    }
}

// DTO classes
data class SkillDto(
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userProfileUrl: String? = null,
    val title: String = "",
    val description: String = "",
    val category: String = "OTHER",
    val level: String = "INTERMEDIATE",
    val isTeaching: Boolean = true,
    val lookingFor: List<String> = emptyList(),
    val availability: String = "",
    val contactPreference: String = "email",
    val phoneNumber: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    fun toDomain(id: String): Skill {
        return Skill(
            id = id,
            userId = userId,
            userName = userName,
            userEmail = userEmail,
            userProfileUrl = userProfileUrl,
            title = title,
            description = description,
            category = try { SkillCategory.valueOf(category) } catch (e: Exception) { SkillCategory.OTHER },
            level = try { SkillLevel.valueOf(level) } catch (e: Exception) { SkillLevel.INTERMEDIATE },
            isTeaching = isTeaching,
            lookingFor = lookingFor.mapNotNull { 
                try { SkillCategory.valueOf(it) } catch (e: Exception) { null }
            },
            availability = availability,
            contactPreference = contactPreference,
            phoneNumber = phoneNumber,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

data class SkillMatchDto(
    val requesterId: String = "",
    val requesterName: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val skillId: String = "",
    val skillTitle: String = "",
    val message: String = "",
    val status: String = "PENDING",
    val createdAt: Long = 0
) {
    fun toDomain(id: String): SkillMatch {
        return SkillMatch(
            id = id,
            requesterId = requesterId,
            requesterName = requesterName,
            providerId = providerId,
            providerName = providerName,
            skillId = skillId,
            skillTitle = skillTitle,
            message = message,
            status = try { MatchStatus.valueOf(status) } catch (e: Exception) { MatchStatus.PENDING },
            createdAt = createdAt
        )
    }
}
