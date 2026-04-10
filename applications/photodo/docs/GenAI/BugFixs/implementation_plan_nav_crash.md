# Fix "NavDisplay backstack cannot be empty" Crash

The goal is to prevent the app from crashing when the navigation backstack becomes empty, which typically happens during certain navigation transitions or when `navigateBack()` is called from a root screen.

## Proposed Changes

### App Components

#### [PhotoDoMainScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/ui/components/PhotoDoMainScreen.kt)

- Refactor `backStack` from `mutableStateListOf` to `mutableStateOf(listOf(...))`. This ensures that updates to the backstack are atomic and prevents intermediate empty states.
- Add safety checks to `onBack` and `navigateBack` to ensure the backstack always contains at least one route.
- Refactor the `navTo` logic in the bottom bar to use atomic state replacement.

```kotlin
    var backStack by remember { mutableStateOf(listOf<PhotoTodoRoute>(PhotoTodoRoute.Home)) }
    val currentRoute = backStack.lastOrNull() ?: PhotoTodoRoute.Home

    Scaffold(
        // ...
        bottomBar = {
            PhotoTodoBottomBar(
                currentRoute = currentRoute,
                navTo = { selectedRoute ->
                    if (currentRoute != selectedRoute) {
                        // 🚀 ATOMIC UPDATE: Ensure backstack is never transiently empty
                        backStack = if (selectedRoute == PhotoTodoRoute.Home) {
                            listOf(PhotoTodoRoute.Home)
                        } else {
                            listOf(PhotoTodoRoute.Home, selectedRoute)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(innerPadding),
            onBack = {
                if (backStack.size > 1) {
                    backStack = backStack.dropLast(1)
                }
            },
            entryProvider = { key ->
                entryProvider(
                    key,
                    windowSizeClass,
                    {
                        if (backStack.size > 1) {
                            backStack = backStack.dropLast(1)
                        }
                    },
                    { dest ->
                        if (dest != backStack.lastOrNull()) {
                            backStack = backStack + dest
                        }
                    }
                )
            }
        )
    }
```

## Verification Plan

### Automated Tests
- Run build to ensure compilation.

### Manual Verification
- Verify that bottom bar navigation still works correctly.
- Verify that deleting the last category in the Tasks tab still navigates home without crashing.
- Verify that pressing the system back button on the Home screen doesn't crash the app (it should ideally minimize the app or do nothing, but not crash).
