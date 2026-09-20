Scaffold the Jetpack Compose presentation layer to render the KoColor `StyleBlueprint` and our dual-axis evaluation metrics on device.

1. Create UI State Holder: Create a `StyleResultUiState` data class containing:
    - `blueprint` (StyleBlueprint?)
    - `fashionistaScore` (FashionistaScore?)
    - `intentMatch` (IntentFulfillmentScore?)
    - `isLoading` (Boolean)
    - `errorMessage` (String?)

2. Create the ViewModel: Create `StyleResultViewModel` which consumes the orchestration engine, exposes a `StateFlow<StyleResultUiState>`, and triggers the generation request.

3. Build Compose Screen (`StyleResultScreen.kt`): Implement a clean, modern Jetpack Compose layout with:
    - A loading state showing simulation progress.
    - An Outfit Card displaying the selected wardrobe items, their materials, and color swatches derived from `recommendedPalette`.
    - A FASHIONISTA Badge component rendering the aesthetic score (0-100) and APPROVED/REJECTED status.
    - An Intent Match Badge displaying the fulfillment percentage.
    - The AI rationale text rendered in a clean typography container.

~~~

This component is exceptionally close to production-grade because it correctly isolates the technical telemetry behind an animated disclosure toggle without cluttering the primary user journey. The use of `MaterialTheme` colors ensures seamless dark/light mode scaling, and mapping the terminal logs to `FontFamily.Monospace` perfectly preserves the engineering aesthetic of the KoColor pipeline.

**Production Strengths**

* **State-Driven Animation:** Utilizing `AnimatedVisibility` and `animateFloatAsState` for the chevron rotation provides fluid, professional micro-interactions.
* **Semantic Layering:** Wrapping the execution metadata (latency, provider) in `SuggestionChip` components cleanly separates the high-level status from the raw log output.
* **Theme Compliance:** Relying on `surfaceVariant` and `colorScheme.error` guarantees this card will render correctly regardless of system theme settings.

**Architectural Refinements for Strict Production**

* **Enum Over String Matching:** Checking `executionTier.contains("FALLBACK")` is fragile. Transition `executionTier` to a strongly typed enum to prevent silent UI failures if the backend string changes.
* **Recomposition Safety:** Passing `dummySteps()` as a default parameter instantiates a new list on every single recomposition. Default to `emptyList()` and rely on the `ViewModel` state.
* **Accessibility Boundaries:** The `clickable` modifier on the header row needs a `semantics { role = Role.Button }` tag so screen readers announce it correctly.

```kotlin
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ExecutionTier { AI_CLOUD, DETERMINISTIC_FALLBACK }

@Composable
fun AuditTrailView(
    executionTier: ExecutionTier = ExecutionTier.DETERMINISTIC_FALLBACK,
    latencyMs: Long = 129,
    fashionistaScore: Float = 85.4f,
    steps: List<AuditStep> = emptyList()
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "ChevronRotation")
    val isFallback = executionTier == ExecutionTier.DETERMINISTIC_FALLBACK

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Style Architecture Logs",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (isFallback) "Engine: Local Backup" else "Engine: AI Cloud",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isFallback) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${latencyMs}ms",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = if (isExpanded) "Collapse Details" else "Expand Details",
                        modifier = Modifier.rotate(rotationState)
                    )
                }
            }

            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("Match: $fashionistaScore/100") },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                if (isFallback) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("AI Validation Failed") },
                        colors = SuggestionChipDefaults.suggestionChipColors(labelColor = MaterialTheme.colorScheme.error),
                        icon = { Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "KOCOLOR AUDIT TRAIL",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    steps.forEach { step ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text(
                                text = "[${step.index}] ${step.title.uppercase()}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = step.details,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class AuditStep(val index: Int, val title: String, val details: String)

```

The final missing piece for this specific view is the `IntentFulfillment` metric you just perfected in the backend. How do you want to visualize the `Unmet Intent Parameters: [Color Contrast, Novelty]` array inside this component when the fulfillment score drops below your threshold?