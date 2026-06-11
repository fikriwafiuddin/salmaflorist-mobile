# Implementation Plan - Refine Product Form Image Handling

This plan outlines improvements to the Admin Product Form, enabling both Camera and Gallery image capture and ensuring that existing images are properly previewed during editing.

## User Review Required

> [!NOTE]
> **Storage Approach**: When capturing images from the camera, I will save them to the app's internal/external storage and store the file path/URI in the database. This ensures the images persist across app restarts.
>
> **Preview Logic**: I will update the fragment to check if the stored image path is a resource name (for seed data), a file path, or a content URI, and display it accordingly in the preview window.

## Proposed Changes

### UI Layer - Admin Product Form

#### [fragment_admin_product_form.xml](file:///D:/project/salmaflorist-mobile/app/src/main/res/layout/fragment_admin_product_form.xml)
- Update the "Pilih Gambar" button section to include two options: **Camera** and **Gallery**.
- I'll use a `LinearLayout` with two icons/buttons or a single button that opens a selection dialog.

#### [AdminProductFormFragment.kt](file:///D:/project/salmaflorist-mobile/app/src/main/java/com/example/salmaflorist/ui/fragment/admin/AdminProductFormFragment.kt)
- **Implement Camera Logic**:
    - Add permissions handling for `Camera` and `Storage` (if needed for older API levels).
    - Implement `ActivityResultLauncher` for `TakePicture` contract.
    - Create a file provider to handle the output URI for the camera.
- **Implement Gallery Logic**:
    - Maintain existing `ACTION_PICK` logic but ensure it correctly updates the preview and stores the URI.
- **Enhance Preview Logic**:
    - In `loadProductData()`, implement a robust image loading helper that handles:
        1. `content://` (Gallery/Picker)
        2. `file://` (Camera/Internal Storage)
        3. Resource Names (e.g., "bunga1" from seed data) using `getIdentifier`.

### Configuration

#### [AndroidManifest.xml](file:///D:/project/salmaflorist-mobile/app/src/main/AndroidManifest.xml)
- Add `<uses-feature android:name="android.hardware.camera" android:required="false" />`.
- Add `<uses-permission android:name="android.permission.CAMERA" />`.
- Add a `<provider>` (FileProvider) to allow the Camera app to write to a file in our app's storage.

#### [NEW] [provider_paths.xml](file:///D:/project/salmaflorist-mobile/app/src/main/res/xml/provider_paths.xml)
- Define the paths used by the FileProvider.

## Verification Plan

### Manual Verification
1.  **Edit Preview**: Open an existing product (both seeded and newly created) and verify the image preview loads immediately.
2.  **Gallery Capture**: Change an image using the Gallery, save, and verify it updates in the list and when re-opening the form.
3.  **Camera Capture**: Use the Camera option, take a photo, verify it appears in the preview, save, and verify persistence.
