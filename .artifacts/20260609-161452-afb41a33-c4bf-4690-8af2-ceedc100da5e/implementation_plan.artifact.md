# Implementation Plan - Admin Role Features

This plan outlines the steps to add an Admin role with its own navigation and dashboard to the SalmaFlorist application.

## User Review Required

> [!IMPORTANT]
> - I will add a `role` column to the `users` table in the database to distinguish between Admin and User.
> - I will create a new activity `AdminMainActivity` for the Admin role to keep the layouting separate and clean, as the Admin role has a different navigation structure and top-level menu.

## Proposed Changes

### Database Layer

#### [DBOpenHelper.kt](file:///D:/project/salmaflorist-mobile/app/src/main/java/com/example/salmaflorist/data/DBOpenHelper.kt)
- Add `USER_ROLE` constant.
- Update `CREATE TABLE users` to include the `role` column.
- Update `addUser` and `checkUser` (or add `getUserRole`) to handle roles.

### UI Layer - Admin Fragments

#### [NEW] [AdminDashboardFragment.kt](file:///D:/project/salmaflorist-mobile/app/src/main/java/com/example/salmaflorist/ui/fragment/admin/AdminDashboardFragment.kt)
- Empty fragment for Admin Dashboard.

#### [NEW] [AdminCategoriesFragment.kt](file:///D:/project/salmaflorist-mobile/app/src/main/java/com/example/salmaflorist/ui/fragment/admin/AdminCategoriesFragment.kt)
- Empty fragment for Managing Categories.

#### [NEW] [AdminProductsFragment.kt](file:///D:/project/salmaflorist-mobile/app/src/main/java/com/example/salmaflorist/ui/fragment/admin/AdminProductsFragment.kt)
- Empty fragment for Managing Products.

#### [NEW] [AdminOrdersFragment.kt](file:///D:/project/salmaflorist-mobile/app/src/main/java/com/example/salmaflorist/ui/fragment/admin/AdminOrdersFragment.kt)
- Empty fragment for Managing Orders.

#### [NEW] [AdminReportsFragment.kt](file:///D:/project/salmaflorist-mobile/app/src/main/java/com/example/salmaflorist/ui/fragment/admin/AdminReportsFragment.kt)
- Empty fragment for Reports.

### UI Layer - Admin Navigation

#### [NEW] [menu_admin_bottom.xml](file:///D:/project/salmaflorist-mobile/app/src/main/res/menu/menu_admin_bottom.xml)
- Bottom navigation menu for Admin (Dashboard, Categories, Products, Orders, Reports).

#### [NEW] [menu_admin_options.xml](file:///D:/project/salmaflorist-mobile/app/src/main/res/menu/menu_admin_options.xml)
- Options menu for Admin Toolbar (Logout).

### UI Layer - Admin Activity

#### [NEW] [AdminMainActivity.kt](file:///D:/project/salmaflorist-mobile/app/src/main/java/com/example/salmaflorist/ui/activity/AdminMainActivity.kt)
- Main entry point for Admin users.
- Handles Bottom Navigation switching between Admin fragments.
- Implements Toolbar with Logout option.

#### [NEW] [activity_admin_main.xml](file:///D:/project/salmaflorist-mobile/app/src/main/res/layout/activity_admin_main.xml)
- Layout for Admin Main Activity with Toolbar and Bottom Navigation.

### Logic - Login Handling

#### [LoginFragment.kt](file:///D:/project/salmaflorist-mobile/app/src/main/java/com/example/salmaflorist/ui/fragment/LoginFragment.kt)
- After successful login, check user role.
- Navigate to `MainActivity` for "user" role and `AdminMainActivity` for "admin" role.

#### [SessionManager.kt](file:///D:/project/salmaflorist-mobile/app/src/main/java/com/example/salmaflorist/util/SessionManager.kt)
- Store `userRole` in SharedPreferences.

## Verification Plan

### Manual Verification
- Register a user and verify they see the standard User UI.
- Manually set a user as 'admin' in the database (or add a hardcoded admin login for testing).
- Verify Admin login redirects to `AdminMainActivity`.
- Verify all Admin Bottom Navigation items load their respective (empty) fragments.
- Verify the Toolbar Logout option works and returns to the Login screen.
