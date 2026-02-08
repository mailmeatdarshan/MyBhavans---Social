package com.bhavans.mybhavans.feature.canteen.domain.repository

import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.canteen.domain.model.Canteen
import com.bhavans.mybhavans.feature.canteen.domain.model.CheckIn
import com.bhavans.mybhavans.feature.canteen.domain.model.CrowdLevel
import kotlinx.coroutines.flow.Flow

interface CanteenRepository {
    
    fun getCanteens(): Flow<Resource<List<Canteen>>>
    
    suspend fun getCanteen(canteenId: String): Resource<Canteen>
    
    fun getRecentCheckIns(canteenId: String, limit: Int = 10): Flow<Resource<List<CheckIn>>>
    
    suspend fun checkIn(
        canteenId: String,
        crowdLevel: CrowdLevel,
        waitTime: Int,
        comment: String = ""
    ): Resource<CheckIn>
    
    suspend fun updateCanteenCrowdLevel(canteenId: String): Resource<Unit>
}
