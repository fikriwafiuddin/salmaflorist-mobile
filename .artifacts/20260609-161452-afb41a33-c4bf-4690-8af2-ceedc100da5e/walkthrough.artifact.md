# Walkthrough - Admin Categories Management

I have implemented the Categories management feature for the Admin role, allowing administrators to view, add, edit, and delete categories directly from a streamlined interface.

## Changes Made

### 1. Data Layer Updates
- **[DBOpenHelper.kt](file:///D:/project/salmaflorist-mobile/app/src/main/java/com/example/salmaflorist/data/DBOpenHelper.kt)**: Added core CRUD methods:
    - `addCategory(name: String)`
    - `updateCategory(id: Int, newName: String)`
    - `deleteCategory(id: Int)`
    - Updated `getAllCategories()` to return categories sorted alphabetically.

### 2. Streamlined UI Design
- **Single-Screen Management**: Instead of separate fragments for forms, I used **AlertDialogs** for adding and editing categories. This keeps the admin on the list view and speeds up their workflow.
- **[fragment_admin_categories.xml](file:///D:/project/salmaflorist-mobile/app/src/main/res/layout/fragment_admin_categories.xml)**:
    - A clean table layout showing category names.
    - A **FloatingActionButton (FAB)** for adding new categories.
- **[item_category_row.xml](file:///D:/project/salmaflorist-mobile/app/src/main/res/layout/item_category_row.xml)**: A custom row layout with a "Settings" icon that triggers a PopupMenu.

### 3. Feature Logic
- **[AdminCategoriesFragment.kt](file:///D:/project/salmaflorist-mobile/app/src/main/java/com/example/salmaflorist/ui/fragment/admin/AdminCategoriesFragment.kt)**:
    - **Dynamic List**: Automatically refreshes the table after any Add, Edit, or Delete operation.
    - **Popup Menu**: Clicking the icon next to a category reveals "Edit" and "Hapus" options.
    - **Validation**: Added basic checks to ensure category names are not empty.
    - **Safety**: Deleting a category includes a confirmation dialog warning that associated products will also be removed (via DB cascade).

## How to Test
1.  **Login as Admin**: Use `admin@gmail.com` / `admin123`.
2.  **Navigate to Categories**: Click the "Categories" icon in the bottom navigation.
3.  **Add Category**: Click the pink `+` button, enter a name, and save.
4.  **Edit/Delete**: Click the slider icon next to any category to reveal the options.

## Verification Summary
- Verified that all CRUD operations correctly update the SQLite database.
- Confirmed that the UI refreshes immediately after database changes.
- Tested the "Edit" pre-filling logic and "Delete" confirmation warnings.
