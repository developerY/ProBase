# Refine Project Merging Logic in Save Photo

Refine the project saving logic to properly merge new information when an existing project name and category are selected.

## User Review Required

> [!IMPORTANT]
> When an existing project is found:
> 1.  **Budgets are summed**: The existing budget and the new input budget will be added together.
> 2.  **Due Date update**: If the new input has a due date, it will overwrite the existing one.
> 3.  **Data Merging**: New tasks and photos will be added to the existing project ID.

## Proposed Changes

### PhotoDo Camera Feature (`features/camera`)

---

#### [SavePhotoViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/features/camera/src/main/java/com/zoewave/probase/photodo/features/camera/ui/SavePhotoViewModel.kt)

- Update `saveTask` logic:
    - If `existingProject` is found:
        - Calculate `newTotalBudget = existingProject.projectBudget + currentInputBudget`.
        - Determine `finalDueDate = currentInputDueDate ?: existingProject.dueDate`.
        - Update the `existingProject` entity with the new budget and due date.
        - Call `repo.updateProject(updatedProject)`.
        - Use `existingProject.projectId` for subsequent task and photo attachments.
    - Otherwise, create a new project as before.

## Verification Plan

### Automated Tests
- Run `gradle_build("app:assembleDebug")` to ensure compilation success.

### Manual Verification
1.  **Merge Check**:
    - Select an existing category and project.
    - Add a new budget amount and a different due date.
    - Save and view the project.
    - Verify that the budget is the sum of old + new.
    - Verify that the due date matches the new input.
    - Verify that new tasks and photos are added to the existing list.
