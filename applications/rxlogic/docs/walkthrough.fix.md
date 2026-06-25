# Walkthrough - Fix Non-Exhaustive 'when' Expression in RxLogicMainScreen

The `RxLogicMainScreen.kt` file had a compilation error because a new route, `RxLogicRoute.MedicationDetail`, was added to the `RxLogicRoute` sealed interface but not handled in the `when` expression within the `entryProvider` of `RxLogicMainScreen`.

## Changes

### [RxLogicMainScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/rxlogic/apps/mobile/src/main/java/com/zoewave/probase/rxlogic/apps/mobile/ui/RxLogicMainScreen.kt)

Added a placeholder branch for `RxLogicRoute.MedicationDetail` to make the `when` expression exhaustive.

```diff
                 RxLogicRoute.Settings -> NavEntry(RxLogicRoute.Settings) {
                     val settingsViewModel: SettingsViewModel = hiltViewModel()
                     val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
                     SettingsScreen(
                         uiState = settingsUiState,
                         onEvent = settingsViewModel::onEvent,
                         navTo = viewModel::navigateTo
                     )
                 }
+                is RxLogicRoute.MedicationDetail -> NavEntry(route) {
+                    Text(
+                        text = "Medication Detail: ${route.medicationId}",
+                        modifier = Modifier.fillMaxSize()
+                    )
+                }
             }
         }
     )
```

## Verification Results

### Automated Tests
- Ran `:applications:rxlogic:apps:mobile:compileDebugKotlin` which now completes successfully.

```
$ ./gradlew :applications:rxlogic:apps:mobile:compileDebugKotlin
BUILD SUCCESSFUL in 2s
```
