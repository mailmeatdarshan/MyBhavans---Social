package com.bhavans.mybhavans.feature.library.domain.repository

import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.library.domain.model.Library
import com.bhavans.mybhavans.feature.library.domain.model.LibraryBook
import com.bhavans.mybhavans.feature.library.domain.model.LibraryMedia
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun getLibrary(): Flow<Resource<Library>>
    fun getNewBooks(limit: Int = 10): Flow<Resource<List<LibraryBook>>>
    fun getTodaysMedia(): Flow<Resource<List<LibraryMedia>>>
}
