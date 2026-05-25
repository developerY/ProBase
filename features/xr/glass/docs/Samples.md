## Key Concepts

- **Spatial UI**: Building interfaces that exist in 3D space rather than just a 2D plane.
- **Glimmer**: The rendering engine used for Glass-based XR experiences.
- **Input Handling**: Managing gaze, gestures, and controller input within the spatial environment.

## Sample Implementations

Refer to `GlassXRDemosPhoneScreen.kt` for the mobile controller implementation. This screen allows users to:
- **Trigger Demos**: Select from a list of available spatial samples.
- **Active Session Control**: Use the bottom bar to stop the current demo or navigate between samples using "Next" and "Previous" controls.

### Available Samples
- **Hello XR**: A basic introduction to spatial placement.
- **Interactive Elements**: Demonstrates gaze and gesture interaction.
- **Spatial Audio**: Showcases 3D sound positioning within the environment.

### Implementation Details

The samples are managed via `GlassXRDemosViewModel`, which tracks the `activeSample` state. When a sample is selected, the phone screen transitions to a "Projecting" state, providing remote control capabilities over the XR experience.

To add a new sample, implement the `XRSample` interface and register it in the `SampleProvider`.

```kotlin
interface XRSample {
    val id: String
    val title: String
    val description: String
    
    fun next(): XRSample
    fun previous(): XRSample
}
```

### Example Implementation

```kotlin
object HelloXRSample : XRSample {
    override val id = "hello_xr"
    override val title = "Hello XR"
    override val description = "A basic introduction to spatial placement."

    override fun next() = InteractiveElementsSample
    override fun previous() = SpatialAudioSample
}
```

### Sample Provider

The `SampleProvider` maintains the registry of all available samples used by the UI.

```kotlin
object SampleProvider {
    val allSamples = listOf(
        HelloXRSample,
        InteractiveElementsSample,
        SpatialAudioSample
    )
}
```

### State Management

The `GlassXRDemosViewModel` exposes the current state to the UI using a `StateFlow`.

```kotlin
class GlassXRDemosViewModel : ViewModel() {
    private val _activeSample = MutableStateFlow<XRSample?>(null)
    val activeSample: StateFlow<XRSample?> = _activeSample.asStateFlow()

    fun updateActiveSample(sample: XRSample?) {
        _activeSample.value = sample
    }
}
```

### UI Implementation (Phone)

The phone UI uses a `Scaffold` with a conditional `bottomBar`. When a sample is active, a control surface appears allowing the user to stop the demo or navigate the sequence.

```kotlin
bottomBar = {
    activeSample?.let { sample ->
        Surface(tonalElevation = 8.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.updateActiveSample(null) }) {
                        Icon(Icons.Default.Close, contentDescription = "Stop Demo")
                    }
                    Text(sample.title, style = MaterialTheme.typography.bodyMedium)
                }
                Row {
                    IconButton(onClick = { viewModel.updateActiveSample(sample.previous()) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                    }
                    IconButton(onClick = { viewModel.updateActiveSample(sample.next()) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                    }
                }
            }
        }
    }
}
```

The main content of the screen displays the list of available samples using a `LazyColumn`. Each item triggers the `updateActiveSample` method when clicked.

```kotlin
LazyColumn(modifier = Modifier.fillMaxSize()) {
    items(SampleProvider.allSamples) { sample ->
        ListItem(
            headlineContent = { Text(sample.title) },
            supportingContent = { Text(sample.description) },
            modifier = Modifier.clickable {
                viewModel.updateActiveSample(sample)
            },
            trailingContent = {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        )
    }
}
```

### Spatial Rendering (Glimmer)

On the XR device, the `SpatialPanel` or `Glimmer` surface observes the `activeSample` from the same `ViewModel`. When a sample is active, it renders the 3D assets associated with that sample.

```kotlin
@Composable
fun GlassXRSpatialContent(viewModel: GlassXRDemosViewModel) {
    val activeSample by viewModel.activeSample.collectAsStateWithLifecycle()

    activeSample?.let { sample ->
        // Spatial components are rendered here
        SpatialLayout {
            // Example of a spatialized component
            GlimmerScene(sampleId = sample.id)
        }
    }
}
```



## Additional Resources

[Code in this feature directory](https://github.com/androidx/androidx/tree/androidx-main/xr/glimmer/glimmer/samples)

### sample code
[Android XR Samples](https://developer.android.com/develop/xr/samples)



For more information on building XR experiences with Jetpack Compose and Glimmer, refer to the following resources:
- [Android [XR Skills](https://github.com/android/skills/tree/main/xr/display-glasses-with-jetpack-compose-glimmer): A collection of samples and best practices for XR development.
- [Jetpack XR SDK Documentation](https://developer.android.com/develop/xr/jetpack-xr-sdk/jetpack-compose-glimmer): Official guide for using Glimmer in spatial environments.
