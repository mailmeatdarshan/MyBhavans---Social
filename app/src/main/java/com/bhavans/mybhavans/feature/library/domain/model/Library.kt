package com.bhavans.mybhavans.feature.library.domain.model

enum class LibraryStatus {
    OPEN,
    CLOSED,
    BUSY
}

data class Library(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val openTime: String = "09:00",
    val closeTime: String = "17:00",
    val status: LibraryStatus = LibraryStatus.OPEN,
    val totalSeats: Int = 150,
    val occupiedSeats: Int = 0,
    val quietZoneSeats: Int = 50,
    val quietZoneOccupied: Int = 0,
    val wifiAvailable: Boolean = true,
    val printingAvailable: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class LibraryBook(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val genre: String = "",
    val issuedDate: Long = System.currentTimeMillis(), // when it was added to library
    val isAvailable: Boolean = true,
    val coverImageUrl: String? = null,
    val description: String = "",
    val totalCopies: Int = 1,
    val availableCopies: Int = 1
)

data class LibraryMedia(
    val id: String = "",
    val title: String = "",
    val publisher: String = "",
    val type: MediaType = MediaType.NEWSPAPER,
    val language: String = "English",
    val isAvailable: Boolean = true,
    val date: Long = System.currentTimeMillis()
)

enum class MediaType {
    NEWSPAPER,
    MAGAZINE
}
