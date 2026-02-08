package com.bhavans.mybhavans.feature.skillswap.domain.repository

import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.skillswap.domain.model.MatchStatus
import com.bhavans.mybhavans.feature.skillswap.domain.model.Skill
import com.bhavans.mybhavans.feature.skillswap.domain.model.SkillCategory
import com.bhavans.mybhavans.feature.skillswap.domain.model.SkillLevel
import com.bhavans.mybhavans.feature.skillswap.domain.model.SkillMatch
import kotlinx.coroutines.flow.Flow

interface SkillSwapRepository {
    
    fun getSkills(
        category: SkillCategory? = null,
        isTeaching: Boolean? = null
    ): Flow<Resource<List<Skill>>>
    
    suspend fun getSkill(skillId: String): Resource<Skill>
    
    fun getUserSkills(userId: String): Flow<Resource<List<Skill>>>
    
    suspend fun createSkill(
        title: String,
        description: String,
        category: SkillCategory,
        level: SkillLevel,
        isTeaching: Boolean,
        lookingFor: List<SkillCategory>,
        availability: String,
        contactPreference: String,
        phoneNumber: String?
    ): Resource<Skill>
    
    suspend fun updateSkill(skill: Skill): Resource<Skill>
    
    suspend fun deleteSkill(skillId: String): Resource<Unit>
    
    suspend fun toggleSkillActive(skillId: String, isActive: Boolean): Resource<Unit>
    
    // Match requests
    suspend fun sendMatchRequest(
        skillId: String,
        message: String
    ): Resource<SkillMatch>
    
    fun getMyMatchRequests(): Flow<Resource<List<SkillMatch>>>
    
    fun getReceivedMatchRequests(): Flow<Resource<List<SkillMatch>>>
    
    suspend fun updateMatchStatus(matchId: String, status: MatchStatus): Resource<Unit>
}
