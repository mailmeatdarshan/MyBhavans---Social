# 🎓 MyBhavans - College Social Media App

A private, high-engagement community app exclusively for students and faculty of Bhavans College. Built with modern Android development practices using Kotlin, Jetpack Compose, and Firebase.

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)

---

## 📱 Features

### 🔐 Secure Authentication
- **College Email Only**: Signup restricted to `@bhavans.ac.in` email domain
- **Email Verification**: Mandatory email verification for account activation
- **Persistent Login**: Stay logged in across app restarts
- **Password Reset**: Forgot password functionality via email

### 📰 Unified Feed (Coming Soon)
- Real-time social feed for campus updates
- Post text, images, and videos
- Like and comment on posts
- Categories: General, Academic, Events, Announcements

### 🛠️ Student Utility Hub (Planned)
- **Lost & Found**: Report and find lost items on campus
- **Canteen Tracker**: Real-time crowd levels in canteens
- **Skill-Swap Marketplace**: Connect with peer tutors

### 🚶 Safety Features (Planned)
- **Safe-Walk Buddy Finder**: Find walking companions for late hours

---

## 🏗️ Architecture

The app follows **Clean Architecture** with **MVVM** pattern:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │   Screens   │  │  ViewModels │  │   UI State/Events   │ │
│  │  (Compose)  │  │   (Hilt)    │  │    (StateFlow)      │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                            │
│  ┌─────────────────────┐  ┌─────────────────────────────┐  │
│  │   Repository        │  │      Domain Models          │  │
│  │   Interfaces        │  │   (User, Post, Comment)     │  │
│  └─────────────────────┘  └─────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                             │
│  ┌─────────────────────┐  ┌─────────────────────────────┐  │
│  │    Repository       │  │      Firebase Services      │  │
│  │  Implementations    │  │  (Auth, Firestore, Storage) │  │
│  └─────────────────────┘  └─────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 📂 Project Structure

```
com.bhavans.mybhavans/
│
├── 📁 core/                          # Shared utilities & configuration
│   ├── 📁 di/                        # Dependency Injection (Hilt)
│   │   ├── FirebaseModule.kt         # Provides Firebase instances
│   │   └── RepositoryModule.kt       # Binds repository implementations
│   ├── 📁 navigation/                # App navigation
│   │   ├── NavGraph.kt               # Navigation graph with routes
│   │   └── Routes.kt                 # Route definitions
│   ├── 📁 ui/theme/                  # Material3 theming
│   │   ├── Color.kt                  # Color palette
│   │   ├── Theme.kt                  # App theme configuration
│   │   └── Type.kt                   # Typography
│   └── 📁 util/                      # Utility classes
│       ├── Constants.kt              # App constants & Firestore paths
│       └── Resource.kt               # Result wrapper (Success/Error/Loading)
│
├── 📁 feature/                       # Feature modules
│   ├── 📁 auth/                      # Authentication feature
│   │   ├── 📁 data/repository/       # Firebase Auth implementation
│   │   ├── 📁 domain/                # User model & repository interface
│   │   └── 📁 presentation/          # Login/SignUp screens & ViewModel
│   ├── 📁 feed/                      # Feed feature (placeholder)
│   ├── 📁 profile/                   # Profile feature (placeholder)
│   ├── 📁 utilities/                 # Utilities hub (placeholder)
│   └── 📁 main/                      # Main screen with bottom navigation
│
├── MainActivity.kt                   # Single Activity entry point
└── MyBhavansApp.kt                   # Application class with @HiltAndroidApp
```

---

## 🔑 Key Components Explained

### Authentication Module

| File | Purpose |
|------|---------|
| `AuthRepository.kt` | Interface defining auth operations (signUp, signIn, signOut, etc.) |
| `AuthRepositoryImpl.kt` | Firebase implementation with email validation and Firestore user creation |
| `AuthViewModel.kt` | Manages auth state, handles user events, coordinates between UI and repository |
| `AuthState.kt` | Data class holding UI state (loading, error, user info, validation flags) |
| `LoginScreen.kt` | Compose UI for login with email/password fields |
| `SignUpScreen.kt` | Compose UI for registration with name, email, password |

### How Authentication Works

```
User taps "Sign Up"
        │
        ▼
┌─────────────────────────────────────┐
│  AuthViewModel.onEvent(SignUp)      │
│  - Validates email domain           │
│  - Validates password length        │
└─────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────┐
│  AuthRepositoryImpl.signUp()        │
│  1. Check @bhavans.ac.in domain     │
│  2. Create Firebase Auth account    │
│  3. Update display name             │
│  4. Create Firestore user document  │
│  5. Send verification email         │
└─────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────┐
│  Returns Resource.Success(User)     │
│  - ViewModel updates AuthState      │
│  - UI navigates to MainScreen       │
└─────────────────────────────────────┘
```

### Navigation Flow

```
App Launch
    │
    ├── User NOT logged in ──► LoginScreen ──► SignUpScreen
    │                                │              │
    │                                └──────┬───────┘
    │                                       │
    │                                       ▼
    │                              (Auth Success)
    │                                       │
    └── User IS logged in ─────────────────►│
                                            ▼
                                      MainScreen
                                    ┌─────┼─────┐
                                    ▼     ▼     ▼
                                  Feed  Utils  Profile
                                   Tab   Tab    Tab
```

### Dependency Injection (Hilt)

| Module | Provides |
|--------|----------|
| `FirebaseModule` | FirebaseAuth, FirebaseFirestore, FirebaseStorage singletons |
| `RepositoryModule` | Binds AuthRepository interface to AuthRepositoryImpl |

---

## 🗄️ Firestore Database Schema

### Users Collection (`/users/{userId}`)

```javascript
{
  uid: "abc123",                    // Firebase Auth UID
  email: "john@bhavans.ac.in",      // College email
  displayName: "John Doe",          // User's name
  photoUrl: "",                     // Profile picture URL
  department: "",                   // Academic department
  year: null,                       // Year of study (1-4)
  role: "student",                  // student | faculty | admin
  bio: "",                          // Short bio
  skills: [],                       // List of skills
  isVerified: false,                // Email verified?
  createdAt: Timestamp,             // Account creation
  lastActiveAt: Timestamp           // Last activity
}
```

---

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| **Kotlin** | Primary language |
| **Jetpack Compose** | Declarative UI framework |
| **Material3** | Design system |
| **Navigation Compose** | Screen navigation |
| **Hilt** | Dependency injection |
| **Firebase Auth** | User authentication |
| **Firebase Firestore** | NoSQL database |
| **Firebase Storage** | File/image storage |
| **Coil** | Image loading |
| **Coroutines + Flow** | Async operations & reactive streams |

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 17
- Android device/emulator (API 26+)
- Firebase project configured

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/MyBhavans.git
   cd MyBhavans
   ```

2. **Firebase Configuration**
   - Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
   - Add Android app with package: `com.bhavans.mybhavans`
   - Download `google-services.json` to `/app/` directory
   - Enable Email/Password authentication
   - Create Firestore database

3. **Build & Run**
   ```bash
   ./gradlew clean build
   ./gradlew installDebug
   ```

---

## 📋 Current Status

| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1: Core Infrastructure | ✅ Complete | Dependencies, DI, theme, navigation |
| Phase 2: Authentication | ✅ Complete | Login, signup, email verification |
| Phase 3: Feed System | 🔄 Pending | Posts, likes, comments |
| Phase 4: Utilities Hub | 📋 Planned | Lost & Found, Canteen, Skills |
| Phase 5: Safety Features | 📋 Planned | Safe-Walk |
| Phase 6: Testing & Polish | 📋 Planned | Tests, security, performance |

---

## 📄 License

This project is for educational purposes as part of Bhavans College.

---

## 👨‍💻 Developer

Made with ❤️ by Pawan
