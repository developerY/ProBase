Here's the markdown version:

# Implementation Plan - Enhanced Health & Wellness Detail Page

## Overview

Enhance the **Health & Wellness Detail Page** to provide a comprehensive view of biological markers, wellness insights, and daily health metrics through deeper integration with **Google Health Connect**.

The updated experience will introduce:

* **Vitals & Alerts**
* **Heart Rate Monitoring**
* **Sleep Tracking**
* **Hydration Tracking**
* **Activity Tracking**
* **Wellness Correlation Insights**
* **Refined Atelier-Inspired Design Language**

---

## 1. Research & Analysis

### Existing Infrastructure

The following systems are already available and can be leveraged:

#### Health Data

* `HealthViewModel`
* `HealthSessionManager`

Currently support:

* Sleep
* Hydration
* Steps
* Calories

via Google Health Connect.

### Alerts Engine

#### WellnessCorrelationEngine

* Exists but is not fully surfaced within the Health Detail Page.
* Produces `SkinInsight` objects that can be presented as health and wellness alerts.

### Vitals Data

#### Heart Rate

`HealthSessionManager` already exposes:

```kotlin
readLatestHeartRateSample()
```

This will be used to populate the new **Vitals** section.

---

## 2. Technical Implementation

### UI Model & State Updates

#### HealthUiState.Success

Update state model to include:

```kotlin
alerts: List<SkinInsight>
latestHeartRate: Long?
todaySteps: Long
todayCalories: Double
```

### ViewModel Enhancements

#### HealthViewModel.initialLoad()

Populate new state fields by:

##### Wellness Analysis

```kotlin
WellnessCorrelationEngine.analyzeTriggers()
```

Inputs:

* Hydration data
* Sleep data

Outputs:

* List<SkinInsight>

##### Heart Rate Retrieval

```kotlin
healthSessionManager.readLatestHeartRateSample()
```

##### Daily Activity Metrics

Retrieve:

* Total steps
* Calories burned

for the current day.

---

## 3. UI Component Refinement

### StyleHealthDashboard.kt

---

### Hydration Card

#### Goals

Match the larger circular visualization shown in the reference design.

#### Features

* Large hydration progress ring
* Daily hydration goal display
* Current intake amount
* Quick Add actions

##### Quick Add Buttons

* +250 ml
* +500 ml

---

### Restorative Rest Card

#### Goals

Match the visual style of the provided reference.

#### Features

* Progress-bar visualization
* Sleep duration
* Sleep quality indication
* Goal completion percentage

---

### Vitals & Alerts Section

#### New Component

Create a dedicated:

**Vitals & Alerts Card**

#### Display Elements

##### Alert Summary

Example:

```text
1 Alerts
```

##### Current Vitals

Examples:

```text
Heart Rate: 72 bpm
```

Future Expansion:

* Resting Heart Rate
* Heart Rate Variability (HRV)
* Respiratory Rate

##### Expandable Alerts

When expanded, display:

```text
Low hydration may impact skin elasticity.
```

```text
Poor sleep recovery detected.
```

```text
Elevated stress indicators observed.
```

Powered by:

```kotlin
List<SkinInsight>
```

---

### Activity Section

Add a dedicated activity dashboard displaying:

#### Daily Steps

Example:

```text
7,842 Steps
```

#### Calories Burned

Example:

```text
534 Calories
```

#### Layout Options

* Two-column grid
* Horizontal metrics row
* Responsive card layout

---

### Element Tracker

#### Refinement Goals

Update the expandable Element Tracker to align with:

* Atelier design language
* Consistent spacing
* Improved visual hierarchy
* Smoother expansion animations

---

## 4. Visual & Aesthetic Standards

### Typography

#### Headings

* Serif typography

#### Primary Metrics

Use:

* Black weight
* Large scale
* High visual emphasis

Examples:

```text
72 bpm
```

```text
2.1 L
```

```text
7h 48m
```

---

### Color System

Use the established Health palette:

| Category  | Color Family |
| --------- | ------------ |
| Sleep     | Purple       |
| Hydration | Blue         |
| Vitals    | Red / Pink   |
| Activity  | Green        |
| Alerts    | Amber        |

---

### Glassmorphism

All major cards should follow the established KoColor visual language.

#### Card Styling

* Subtle transparency
* Soft blur effects
* Rounded corners

```kotlin
RoundedCornerShape(32.dp)
```

#### Additional Styling

* Elevated shadows
* Layered depth
* Smooth animations

---

## 5. Expected Outcome

The redesigned Health & Wellness page will become a comprehensive wellness dashboard that combines:

### Biological Vitals

* Heart Rate
* Future biometric metrics

### Wellness Intelligence

* SkinInsight alerts
* WellnessCorrelationEngine recommendations

### Lifestyle Tracking

* Sleep
* Hydration
* Activity
* Calories

### Enhanced Visual Experience

* Large hydration visualization
* Restorative rest dashboard
* Vitals & Alerts center
* Atelier-inspired design consistency

The result is a richer, more actionable wellness experience that surfaces both raw health metrics and personalized wellness insights in a premium, visually cohesive interface.
