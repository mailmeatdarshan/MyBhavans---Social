# 🎓 MyBhavans — College Social Media App

A **private**, high-engagement community app exclusively for students and faculty of **Bhavans College**. Built with modern Android development using Kotlin, Jetpack Compose, and a Clean Architecture + MVVM pattern.

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Hilt](https://img.shields.io/badge/Hilt-DI-orange?style=for-the-badge)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26-green?style=for-the-badge)

---

## 📱 Features

### 🔐 Secure Authentication ✅
- **College Email Only** — signup restricted to `@bhavans.ac.in` domain
- **Email Verification** — mandatory verification before accessing the app
- **Persistent Session** — stay logged in across app restarts
- **Password Reset** — email-based forgot-password flow
- **Profile Management** — update display name, bio, department, year, and profile photo

### 📰 Unified Feed ✅
- Real-time social feed for campus updates
- Create, edit, and delete posts (text + image)
- **Like & Comment** on posts with real-time counts
- **Categories**: General, Academic, Events, Announcements
- Post detail screen with full comment thread

### 🔍 Explore ✅
- Browse and discover content across categories

### 🛠️ Student Utility Hub

| Feature | Status |
|---------|--------|
| 🏷️ Lost & Found | ✅ Implemented |
| 🍽️ Canteen Tracker | ✅ Implemented |
| 🔄 Skill-Swap Marketplace | ✅ Implemented |

#### 🏷️ Lost & Found ✅
- Report **Lost** or **Found** items with photos
- Category filtering (Electronics, Accessories, Documents, etc.)
- Mark items as **Resolved** when found
- Contact info attached to each listing

#### 🍽️ Canteen Tracker ✅
- Real-time crowd/status updates for campus canteens
- Helps students plan meal timings

#### 🔄 Skill-Swap Marketplace ✅
- Post skills you **teach** or skills you **want to learn**
- Filter by skill category and level
- Send and receive **Match Requests** with custom messages
- Accept/decline skill-swap requests

### 🚶 Safe-Walk ✅
- Find walking companions during late hours on campus
- Real-time session management for safety coordination

### 🛡️ Admin Panel ✅
- Role-based access for college administrators
- Content moderation and user management

---

## 🏗️ Architecture

The app follows **Clean Architecture** with **MVVM** across three clear layers:

```
┌─────────────────────────────────────────────────────────┐
│                   Presentation Layer                     │
│  Compose Screens  ◄──►  ViewModels (Hilt)               │
│                          ◄──► UI State (StateFlow)       │
└─────────────────────────────┬───────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────┐
│                     Domain Layer                         │
│  Repository Interfaces  +  Domain Models                 │
│  (User, Post, Skill, LostFoundItem, etc.)               │
└─────────────────────────────┬───────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────┐
│                      Data Layer                          │
│  Repository Implementations  +  Backend Services         │
│  (Auth, Database, File Storage)                          │
└─────────────────────────────────────────────────────────┘
```

---

## 📂 Project Structure

```
com.bhavans.mybhavans/
│
├── 📁 core/
│   ├── 📁 di/                   # Hilt DI modules
│   │   ├── FirebaseModule.kt    # Provides backend client(s)
│   │   └── RepositoryModule.kt  # Binds repo interfaces to impls
│   ├── 📁 navigation/
│   │   ├── NavGraph.kt          # Navigation graph & routes
│   │   └── Routes.kt
│   ├── 📁 ui/theme/             # Material3: Color, Theme, Type
│   └── 📁 util/
│       ├── Constants.kt         # DB collection paths, roles
│       └── Resource.kt          # Result wrapper (Success/Error/Loading)
│
├── 📁 feature/
│   ├── 📁 auth/                 # Signup, Login, Profile
│   ├── 📁 feed/                 # Posts, Likes, Comments
│   ├── 📁 explore/              # Content discovery
│   ├── 📁 lostfound/            # Lost & Found listings
│   ├── 📁 canteen/              # Canteen crowd tracker
│   ├── 📁 skillswap/            # Skill-Swap Marketplace
│   ├── 📁 safewalk/             # Safe-Walk sessions
│   ├── 📁 admin/                # Admin moderation panel
│   ├── 📁 activity/             # Activity/notification feed
│   ├── 📁 profile/              # User profile screen
│   ├── 📁 utilities/            # Utilities hub screen
│   └── 📁 main/                 # Main screen + bottom nav
│
├── MainActivity.kt              # Single-Activity entry point
└── MyBhavansApp.kt              # @HiltAndroidApp Application class
```

---

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| **Kotlin** | Primary language |
| **Jetpack Compose** | Declarative UI |
| **Material3** | Design system |
| **Navigation Compose** | Screen routing |
| **Hilt** | Dependency injection |
| **Coroutines + Flow** | Async & reactive streams |
| **Coil** | Image loading |
| **DataStore** | Local preference storage |
| **Firebase Auth** | User authentication |
| **Firebase Firestore** | Real-time NoSQL database |
| **Cloudinary** | Image/file storage (free 25 GB tier) |
| **Firebase Messaging** | Push notifications |

---

## 🗄️ Database Schema

### Users (`/users/{uid}`)
```
uid, email, displayName, photoUrl, department, year,
role (student|faculty|admin), bio, gender, skills[],
isVerified, postsCount, followersCount, followingCount,
createdAt, lastActiveAt
```

### Posts (`/posts/{postId}`)
```
authorId, authorName, authorPhotoUrl, content, imageUrl,
category (GENERAL|ACADEMIC|EVENTS|ANNOUNCEMENTS),
likes[], commentCount, createdAt, updatedAt
```

### Comments (`/posts/{postId}/comments/{commentId}`)
```
authorId, authorName, authorPhotoUrl, content, createdAt
```

### Lost & Found (`/lostfound/{itemId}`)
```
title, description, type (LOST|FOUND), category,
location, imageUrl, authorId, authorName, contactNumber,
isResolved, createdAt
```

### Skills (`/skills/{skillId}`)
```
userId, userName, title, description, category, level,
isTeaching, lookingFor[], availability, contactPreference,
isActive, createdAt, updatedAt
```

### Skill Matches (`/skill_matches/{matchId}`)
```
requesterId, requesterName, providerId, providerName,
skillId, skillTitle, message, status (PENDING|ACCEPTED|REJECTED),
createdAt
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (or newer)
- JDK 17
- Android device / emulator (API 26+)
- Firebase project with Auth and Firestore enabled
- Cloudinary account (free tier)

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/mailmeatdarshan/MyBhavans.git
   cd MyBhavans
   ```

2. **Configure Firebase**
   - Go to [console.firebase.google.com](https://console.firebase.google.com)
   - Create a project → Add Android app with package `com.bhavans.mybhavans`
   - Download `google-services.json` → place it in `/app/`
   - Enable **Email/Password** authentication
   - Create a Firestore database (start in test mode, then apply security rules)

3. **Configure Cloudinary** (replaces Firebase Storage — free 25 GB tier)
   - Sign up at [cloudinary.com](https://cloudinary.com) (free)
   - In your Cloudinary Dashboard → Settings → Upload → create an **unsigned upload preset** named `mybhavans_unsigned`
   - Copy your **Cloud name** from the dashboard
   - Open `Constants.kt` and replace:
     ```kotlin
     const val CLOUDINARY_CLOUD_NAME = "YOUR_CLOUD_NAME"  // ← your cloud name here
     ```

4. **Build & Run**
   ```bash
   ./gradlew clean assembleDebug
   ./gradlew installDebug
   ```

---

## 📋 Development Status

| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1: Core Infrastructure | ✅ Complete | DI, theme, navigation, utilities |
| Phase 2: Authentication | ✅ Complete | Signup, login, email verification, profile |
| Phase 3: Unified Feed | ✅ Complete | Posts, likes, comments, real-time updates |
| Phase 4: Utilities Hub | ✅ Complete | Lost & Found, Canteen, Skill-Swap |
| Phase 5: Safety Features | ✅ Complete | Safe-Walk buddy sessions |
| Phase 6: Admin Panel | ✅ Complete | Moderation dashboard |
| Phase 7: Testing & Polish | 🔄 In Progress | Tests, security rules, performance |

---

## 🔒 Security Notes

- All users must use a verified `@bhavans.ac.in` email — non-college signups are rejected at the app layer
- Firestore Security Rules should restrict reads/writes to authenticated users only
- Profile photos are stored in Firebase Storage with user-scoped paths

---

## 📄 License

This project is developed for educational purposes as part of Bhavans College.

---

## 👨‍💻 Developer

Made with ❤️ by **Dubey** — [github.com/mailmeatdarshan](https://github.com/mailmeatdarshan)
