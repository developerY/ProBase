# Add Dropdown Selection for Category and Project in Save Photo

The goal is to improve the user experience when saving a project by providing dropdown menus for "Category Name" and "Project Name". This allows users to easily reuse existing categories and projects while still supporting AI-provided values, quick-set icons, and manual typing.

## User Review Required

> [!NOTE]
> The dropdowns will be implemented using `ExposedDropdownMenuBox` which supports both manual text entry (editable) and selection from a list.

## Proposed Changes

### PhotoDo Camera Feature (`features/camera`)

---

#### [SavePhotoViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/features/camera/src/main/java/com/zoewave/probase/photodo/features/camera/ui/SavePhotoViewModel.kt)

- Update the `uiState` combination logic to include `repo.getAllProjects()`.
- Map the resulting projects list into the `SavePhotoUiState`.

#### [SavePhotoBottomSheet.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/features/camera/src/main/java/com/zoewave/probase/photodo/features/camera/ui/components/SavePhotoBottomSheet.kt)

- **Category Selection**: Refine the `ExposedDropdownMenuBox` to ensure it correctly handles manual typing and AI-generated state.
- **Project Selection**: Wrap the "Project Name" `AiEnhancedTextField` in an `ExposedDropdownMenuBox`.
- Populate both dropdowns with unique names from `uiState.categories` and `uiState.projects` respectively.

---

## Verification Plan

### Automated Tests
- Run `gradle_build("app:assembleDebug")` to ensure compilation success.

### Manual Verification
- Perform a Smart Capture analysis or type a task command.
- On the save screen:
    - Verify that clicking the "Category Name" field shows a dropdown of existing categories.
    - Verify that selecting a category from the dropdown updates the field.
    - Verify that clicking the "Project Name" field shows a dropdown of existing projects.
    - Verify that selecting a project from the dropdown updates the field.
    - Verify that manual typing still works for both fields.
    - Verify that quick-set icons still work for both fields.
