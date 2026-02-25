package com.bhavans.mybhavans.feature.library.data.repository

import com.bhavans.mybhavans.core.util.Resource
import com.bhavans.mybhavans.feature.library.domain.model.Library
import com.bhavans.mybhavans.feature.library.domain.model.LibraryBook
import com.bhavans.mybhavans.feature.library.domain.model.LibraryMedia
import com.bhavans.mybhavans.feature.library.domain.model.LibraryStatus
import com.bhavans.mybhavans.feature.library.domain.model.MediaType
import com.bhavans.mybhavans.feature.library.domain.repository.LibraryRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : LibraryRepository {

    private val libraryCollection = firestore.collection("library")
    private val booksCollection = firestore.collection("library_books")
    private val mediaCollection = firestore.collection("library_media")

    override fun getLibrary(): Flow<Resource<Library>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = libraryCollection
            .document("bhavans_library")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load library status"))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val library = Library(
                        id = snapshot.id,
                        name = snapshot.getString("name") ?: "Bhavans Library",
                        location = snapshot.getString("location") ?: "First Floor, Main Building",
                        openTime = snapshot.getString("openTime") ?: "09:00",
                        closeTime = snapshot.getString("closeTime") ?: "17:00",
                        status = try {
                            LibraryStatus.valueOf(snapshot.getString("status") ?: "OPEN")
                        } catch (e: Exception) { LibraryStatus.OPEN },
                        totalSeats = snapshot.getLong("totalSeats")?.toInt() ?: 150,
                        occupiedSeats = snapshot.getLong("occupiedSeats")?.toInt() ?: 0,
                        quietZoneSeats = snapshot.getLong("quietZoneSeats")?.toInt() ?: 50,
                        quietZoneOccupied = snapshot.getLong("quietZoneOccupied")?.toInt() ?: 0,
                        wifiAvailable = snapshot.getBoolean("wifiAvailable") ?: true,
                        printingAvailable = snapshot.getBoolean("printingAvailable") ?: true,
                        lastUpdated = snapshot.getLong("lastUpdated") ?: System.currentTimeMillis()
                    )
                    trySend(Resource.Success(library))
                } else {
                    // Seed the default library document
                    seedDefaultLibrary()
                    trySend(Resource.Success(defaultLibrary()))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun getNewBooks(limit: Int): Flow<Resource<List<LibraryBook>>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = booksCollection
            .orderBy("issuedDate", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load books"))
                    return@addSnapshotListener
                }

                val books = snapshot?.documents?.mapNotNull { doc ->
                    runCatching {
                        LibraryBook(
                            id = doc.id,
                            title = doc.getString("title") ?: return@mapNotNull null,
                            author = doc.getString("author") ?: "",
                            genre = doc.getString("genre") ?: "General",
                            issuedDate = doc.getLong("issuedDate") ?: System.currentTimeMillis(),
                            isAvailable = doc.getBoolean("isAvailable") ?: true,
                            coverImageUrl = doc.getString("coverImageUrl"),
                            description = doc.getString("description") ?: "",
                            totalCopies = doc.getLong("totalCopies")?.toInt() ?: 1,
                            availableCopies = doc.getLong("availableCopies")?.toInt() ?: 1
                        )
                    }.getOrNull()
                } ?: emptyList()

                if (books.isEmpty()) seedDefaultBooks()

                trySend(Resource.Success(books))
            }

        awaitClose { listener.remove() }
    }

    override fun getTodaysMedia(): Flow<Resource<List<LibraryMedia>>> = callbackFlow {
        trySend(Resource.Loading())

        val todayStart = run {
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }

        // Fetch all media, filter today's client-side to avoid compound index
        val listener = mediaCollection
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load media"))
                    return@addSnapshotListener
                }

                val allMedia = snapshot?.documents?.mapNotNull { doc ->
                    runCatching {
                        LibraryMedia(
                            id = doc.id,
                            title = doc.getString("title") ?: return@mapNotNull null,
                            publisher = doc.getString("publisher") ?: "",
                            type = try {
                                MediaType.valueOf(doc.getString("type") ?: "NEWSPAPER")
                            } catch (e: Exception) { MediaType.NEWSPAPER },
                            language = doc.getString("language") ?: "English",
                            isAvailable = doc.getBoolean("isAvailable") ?: true,
                            date = doc.getLong("date") ?: System.currentTimeMillis()
                        )
                    }.getOrNull()
                } ?: emptyList()

                // Show today's + recent (last 2 days) since "today" in a college context can be fresh
                val recentCutoff = todayStart - (2 * 24 * 60 * 60 * 1000L)
                val todaysMedia = allMedia.filter { it.date >= recentCutoff }

                if (allMedia.isEmpty()) seedDefaultMedia()

                trySend(Resource.Success(if (todaysMedia.isEmpty()) allMedia.take(8) else todaysMedia))
            }

        awaitClose { listener.remove() }
    }

    // ── Seed helpers ─────────────────────────────────────────────────────────

    private fun defaultLibrary() = Library(
        id = "bhavans_library",
        name = "Bhavans Library",
        location = "First Floor, Main Building",
        status = LibraryStatus.OPEN,
        totalSeats = 150,
        occupiedSeats = 45,
        quietZoneSeats = 50,
        quietZoneOccupied = 12
    )

    private fun seedDefaultLibrary() {
        libraryCollection.document("bhavans_library").set(
            mapOf(
                "name" to "Bhavans Library",
                "location" to "First Floor, Main Building",
                "openTime" to "09:00",
                "closeTime" to "17:00",
                "status" to "OPEN",
                "totalSeats" to 150,
                "occupiedSeats" to 45,
                "quietZoneSeats" to 50,
                "quietZoneOccupied" to 12,
                "wifiAvailable" to true,
                "printingAvailable" to true,
                "lastUpdated" to System.currentTimeMillis()
            )
        )
    }

    private fun seedDefaultBooks() {
        val now = System.currentTimeMillis()
        val day = 24 * 60 * 60 * 1000L
        val books = listOf(
            mapOf("title" to "Clean Code", "author" to "Robert C. Martin", "genre" to "Technology", "description" to "A handbook of agile software craftsmanship.", "issuedDate" to now, "isAvailable" to true, "totalCopies" to 3, "availableCopies" to 2),
            mapOf("title" to "The Alchemist", "author" to "Paulo Coelho", "genre" to "Fiction", "description" to "A mystical story of Santiago, an Andalusian shepherd boy.", "issuedDate" to (now - day), "isAvailable" to true, "totalCopies" to 2, "availableCopies" to 2),
            mapOf("title" to "Atomic Habits", "author" to "James Clear", "genre" to "Self-Help", "description" to "Tiny changes, remarkable results.", "issuedDate" to (now - 2 * day), "isAvailable" to false, "totalCopies" to 2, "availableCopies" to 0),
            mapOf("title" to "Deep Work", "author" to "Cal Newport", "genre" to "Self-Help", "description" to "Rules for focused success in a distracted world.", "issuedDate" to (now - 3 * day), "isAvailable" to true, "totalCopies" to 1, "availableCopies" to 1),
            mapOf("title" to "Introduction to Algorithms", "author" to "CLRS", "genre" to "Technology", "description" to "Comprehensive coverage of algorithms and data structures.", "issuedDate" to (now - 4 * day), "isAvailable" to true, "totalCopies" to 4, "availableCopies" to 3),
            mapOf("title" to "Wings of Fire", "author" to "A.P.J. Abdul Kalam", "genre" to "Biography", "description" to "An autobiography of India's beloved scientist and president.", "issuedDate" to (now - 5 * day), "isAvailable" to true, "totalCopies" to 3, "availableCopies" to 1),
            mapOf("title" to "Rich Dad Poor Dad", "author" to "Robert Kiyosaki", "genre" to "Finance", "description" to "What the rich teach their kids about money.", "issuedDate" to (now - 6 * day), "isAvailable" to true, "totalCopies" to 2, "availableCopies" to 2),
            mapOf("title" to "Harry Potter & the Sorcerer's Stone", "author" to "J.K. Rowling", "genre" to "Fiction", "description" to "A young boy discovers he is a wizard.", "issuedDate" to (now - 7 * day), "isAvailable" to false, "totalCopies" to 2, "availableCopies" to 0)
        )
        books.forEach { booksCollection.add(it) }
    }

    private fun seedDefaultMedia() {
        val now = System.currentTimeMillis()
        val media = listOf(
            mapOf("title" to "The Times of India", "publisher" to "Bennett, Coleman & Co.", "type" to "NEWSPAPER", "language" to "English", "isAvailable" to true, "date" to now),
            mapOf("title" to "Maharashtra Times", "publisher" to "Bennett, Coleman & Co.", "type" to "NEWSPAPER", "language" to "Marathi", "isAvailable" to true, "date" to now),
            mapOf("title" to "Hindustan Times", "publisher" to "HT Media", "type" to "NEWSPAPER", "language" to "English", "isAvailable" to true, "date" to now),
            mapOf("title" to "Lokmat", "publisher" to "Lokmat Media", "type" to "NEWSPAPER", "language" to "Marathi", "isAvailable" to true, "date" to now),
            mapOf("title" to "India Today", "publisher" to "Living Media India", "type" to "MAGAZINE", "language" to "English", "isAvailable" to true, "date" to now),
            mapOf("title" to "Outlook", "publisher" to "Outlook Publishing", "type" to "MAGAZINE", "language" to "English", "isAvailable" to false, "date" to now),
            mapOf("title" to "Competition Success Review", "publisher" to "CSR", "type" to "MAGAZINE", "language" to "English", "isAvailable" to true, "date" to now),
            mapOf("title" to "Digit", "publisher" to "9.9 Media", "type" to "MAGAZINE", "language" to "English", "isAvailable" to true, "date" to now)
        )
        media.forEach { mediaCollection.add(it) }
    }
}
