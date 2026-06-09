# Implementation Plan - Admin Categories Management

This plan outlines the implementation of the Categories management feature for the Admin role.

## User Review Required

> [!TIP]
> **Form Design**: Instead of creating a new fragment for the "Add Category" form, I propose using a **BottomSheetDialogFragment** or a **MaterialAlertDialog**. This provides a smoother user experience as the admin doesn't have to leave the list view to add or edit a category. I will proceed with an `AlertDialog` containing a custom view for simplicity and speed.

## Proposed Changes

### Data Layer

#### [DBOpenHelper.kt](file:///D:/project/salmaflorist-mobile/app/src/main/java/com/example/salmaflorist/data/DBOpenHelper.kt)
- Add `addCategory(name: String)` method.
- Add `updateCategory(id: Int, newName: String)` method.
- Add `deleteCategory(id: Int)` method.

### UI Layer - Admin Categories

#### [fragment_admin_categories.xml](file:///D:/project/salmaflorist-mobile/app/src/main/res/layout/fragment_admin_categories.xml)
- Update layout to include:
    - A "Add Category" button (FloatingActionButton or standard Button).
    - A table header for the category list.
    - A `TableLayout` to dynamically display the categories.

#### [NEW] [item_category_row.xml](file:///D:/project/salmaflorist-mobile/app/src/main/res/layout/item_category_row.xml)
- Layout for a single row in the category table (Category Name + Menu Icon).

#### [AdminCategoriesFragment.kt](file:///D:/project/salmaflorist-mobile/app/src/main/java/com/example/salmaflorist/ui/fragment/admin/AdminCategoriesFragment.kt)
- Implement logic to:
    - Load categories from the database.
    - Dynamically populate the `TableLayout`.
    - Handle the "Add" button click to show an input dialog.
    - Handle the "Menu" icon click to show a `PopupMenu` with Edit and Delete options.
    - Perform Edit (show dialog with pre-filled name) and Delete (show confirmation) actions.

## Verification Plan

### Manual Verification
1.  **View List**: Navigate to Categories and verify all seeded categories are displayed.
2.  **Add Category**: Click the "Add" button, enter a name, and verify the list updates.
3.  **Edit Category**: Click the menu on a category, select "Edit", change the name, and verify it updates in the database and UI.
4.  **Delete Category**: Click the menu, select "Delete", confirm, and verify the category is removed.
