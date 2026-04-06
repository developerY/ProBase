# Refine Journey Test Script

Refine the `main_flow.journey.xml` script to better follow the "Tips for writing journeys" provided in the Android Gemini documentation.

## Proposed Changes

### Journey Test Script

#### [main_flow.journey.xml](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/journeysTest/resources/com/zoewave/probase/photodo/mobile/main_flow.journey.xml)

- Update steps to use more precise language and include clearer success criteria.
- Ensure all steps are granular and intentional.

```xml
<journey name="Core Flow" description="Tests Category, Project, and Task creation">
    <step description="Tap 'View All Categories' on the home screen to navigate to the categories list." />
    <step description="Open the add menu by tapping the '+' button at the bottom right of the screen." />
    <step description="Select 'New Category' from the menu to open the category creation sheet." />
    <step description="Type 'Work' into the text field labeled 'Category Name'." />
    <step description="Tap 'Create Category'. This should close the sheet and you should see 'Work' displayed at the top of the screen." />
    <step description="Open the category add menu by tapping the '+' button again." />
    <step description="Select 'New Project' from the menu." />
    <step description="Type 'Launch Website' into the field for the project name." />
    <step description="Tap 'Create'. This should return you to the category screen where 'Launch Website' should now be listed as a project." />
    <step description="Click on the 'Launch Website' project card to open the project detail view." />
    <step description="Open the project action menu by tapping the '+' button." />
    <step description="Select 'Add Task' to open the task entry dialog." />
    <step description="Type 'Buy domain' in the input field and then tap 'Add' to save the task." />
    <step description="Verify that the task 'Buy domain' is now visible in the checklist section." />
</journey>
```

## Verification Plan

### Automated Tests
- Run `./gradlew :applications:photodo:apps:mobile:assembleDebug` to ensure the project still builds.

### Manual Verification
- The user can open the refined `.journey.xml` file in Android Studio and run it to verify the AI's improved navigation.
