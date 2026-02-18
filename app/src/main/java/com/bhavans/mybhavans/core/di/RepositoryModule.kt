package com.bhavans.mybhavans.core.di

import com.bhavans.mybhavans.feature.admin.data.repository.AdminRepositoryImpl
import com.bhavans.mybhavans.feature.admin.domain.repository.AdminRepository
import com.bhavans.mybhavans.feature.auth.data.repository.AuthRepositoryImpl
import com.bhavans.mybhavans.feature.auth.domain.repository.AuthRepository
import com.bhavans.mybhavans.feature.canteen.data.repository.CanteenRepositoryImpl
import com.bhavans.mybhavans.feature.canteen.domain.repository.CanteenRepository
import com.bhavans.mybhavans.feature.feed.data.repository.FeedRepositoryImpl
import com.bhavans.mybhavans.feature.feed.domain.repository.FeedRepository
import com.bhavans.mybhavans.feature.lostfound.data.repository.LostFoundRepositoryImpl
import com.bhavans.mybhavans.feature.lostfound.domain.repository.LostFoundRepository
import com.bhavans.mybhavans.feature.safewalk.data.repository.SafeWalkRepositoryImpl
import com.bhavans.mybhavans.feature.safewalk.domain.repository.SafeWalkRepository
import com.bhavans.mybhavans.feature.skillswap.data.repository.SkillSwapRepositoryImpl
import com.bhavans.mybhavans.feature.skillswap.domain.repository.SkillSwapRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindFeedRepository(
        feedRepositoryImpl: FeedRepositoryImpl
    ): FeedRepository

    @Binds
    @Singleton
    abstract fun bindLostFoundRepository(
        lostFoundRepositoryImpl: LostFoundRepositoryImpl
    ): LostFoundRepository

    @Binds
    @Singleton
    abstract fun bindCanteenRepository(
        canteenRepositoryImpl: CanteenRepositoryImpl
    ): CanteenRepository

    @Binds
    @Singleton
    abstract fun bindSkillSwapRepository(
        skillSwapRepositoryImpl: SkillSwapRepositoryImpl
    ): SkillSwapRepository

    @Binds
    @Singleton
    abstract fun bindSafeWalkRepository(
        safeWalkRepositoryImpl: SafeWalkRepositoryImpl
    ): SafeWalkRepository

    @Binds
    @Singleton
    abstract fun bindAdminRepository(
        adminRepositoryImpl: AdminRepositoryImpl
    ): AdminRepository
}
