# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SalmaFlorist is an Android e-commerce application for a flower shop built with Kotlin. The app serves two user types:

- **Customers**: Browse products, manage cart, place orders, view order history
- **Admins**: Manage products, categories, orders, and view reports

The app integrates with a REST API (`https://salmaflorist-api.vercel.app/`) for backend operations.

## Build and Run

```bash
# Build the project
./gradlew build

# Install debug APK to connected device/emulator
./gradlew installDebug

# Run tests
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests
```

## Architecture

### Layer Structure

```
app/src/main/java/com/example/salmaflorist/
├── data/
│   ├── api/
│   │   ├── ApiConfig.kt          # Retrofit/OkHttp configuration
│   │   ├── ApiService.kt         # REST API endpoint definitions
│   │   └── dto/                   # Request/Response DTOs, ApiResult wrapper
│   └── repository/               # Repository pattern for data access
├── model/                        # Legacy SQLite models (being migrated)
├── ui/
│   ├── activity/                 # Activities (MainActivity, AdminMainActivity)
│   ├── fragment/                 # User-facing fragments
│   └── adapter/                  # RecyclerView adapters
└── util/
    └── SessionManager.kt         # SharedPreferences for auth/session
```

### Key Patterns

**Repository Pattern**: Repositories (`AuthRepository`, `ProductRepository`, `CartRepository`, `OrderRepository`) handle API calls and return `ApiResult<T>` sealed class (Success/Error/Loading).

**Session Management**: `SessionManager` uses SharedPreferences to store JWT token and user data. After login, check `sessionManager.getUserRole()` to determine if user is admin.

**Auth Flow**: 
1. `LoginFragment` → `AuthRepository.login()` → API
2. On success: `SessionManager.createLoginSession()` stores token + user data
3. MainActivity checks role: redirects to `AdminMainActivity` for admins, `HomeFragment` for users

**API Integration**:
- Retrofit with Gson converter
- JWT token injected via `ApiConfig.getApiServiceWithAuth(tokenProvider)`
- Repositories accept `tokenProvider: () -> String?` lambda to get current token
- All network calls use `suspend` functions with coroutines

### Dependencies

- **Networking**: Retrofit 2.9.0, OkHttp 4.12.0, Gson 2.10.1
- **Async**: Kotlin Coroutines 1.7.3
- **UI**: ViewBinding, Navigation Component, Material Design, RecyclerView
- **Image Loading**: Glide 4.16.0

## User Roles

- **USER**: Can browse products, add to cart, checkout, view orders
- **ADMIN**: Has access to admin panel (AdminMainActivity) with Dashboard, Categories, Products, Orders, Reports

## Key Files

- `ApiConfig.kt` - Base URL and Retrofit client configuration
- `ApiService.kt` - All REST endpoint definitions with documentation
- `SessionManager.kt` - Auth state management
- `MainActivity.kt` - Entry point, handles login flow and bottom navigation
- `AdminMainActivity.kt` - Admin panel entry point

## Navigation

User flow: Bottom navigation (Home, Catalog, Cart, Profile)
Admin flow: Bottom navigation (Dashboard, Categories, Products, Orders, Reports)
