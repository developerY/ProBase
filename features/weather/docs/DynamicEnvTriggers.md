# Implementation Plan - Open-Meteo & Dynamic Environmental Triggers

## Overview

Complete the **Environmental Context** domain by integrating the **Open-Meteo API (Privacy-First)** and leveraging its hyper-local environmental data (**UV Index, Humidity, Temperature**) to drive dynamic UI behavior throughout KoColor.

---

## 1. Network Layer (`:core:network`)

### Open-Meteo Integration

* [ ] Define `OpenMeteoService` Retrofit interface.

    * Endpoint:

        * `https://api.open-meteo.com/v1/forecast`
    * Parameters:

        * `latitude`
        * `longitude`
        * `current=temperature_2m,relative_humidity_2m,is_day,weather_code,uv_index`
        * `timezone=auto`

### Data Transfer Objects

* [ ] Create DTOs (`OpenMeteoResponse`) to parse API JSON responses.

### Repository Updates

* [ ] Update `WeatherRepo` interface:

```kotlin
suspend fun getEnvironmentalContext(
    lat: Double,
    lon: Double
): EnvironmentalContext?
```

### Domain Models

* [ ] Implement `EnvironmentalContext` data class in `:core:model`.

### Repository Implementation

* [ ] Implement `OpenMeteoRepoImpl`.
* [ ] Register implementation with Hilt dependency injection.

---

## 2. Feature Layer (`:features:weather`)

### Use Case Updates

* [ ] Update `GetCurrentWeatherUseCase` to retrieve data from Open-Meteo.

### Domain Model Enhancements

* [ ] Extend `WeatherInfo` to include:

    * `uvIndex`
    * `humidity`

### ViewModel Updates

* [ ] Update `WeatherViewModel` to handle the expanded environmental data stream.

---

## 3. Application Layer (`:applications:kocolor:apps:mobile`)

### Home Experience

* [ ] Update `HomeViewModel` to fetch real-time environmental context.

### Environmental Trigger Engine

#### High UV Trigger

* [ ] If `uvIndex > 3.0`:

    * Prioritize SPF recommendations.
    * Surface SPF-focused beauty tips.
    * Highlight sunscreen-related routines.

#### Low Humidity Trigger

* [ ] If `humidity < 30%`:

    * Recommend hydration-focused skincare.
    * Elevate products containing:

        * Hyaluronic Acid
        * Ceramides
        * Heavy Moisturizers
    * Surface hydration-related beauty guidance.

### Cosmetic Detail Experience

* [ ] Update `CosmeticDetailScreen` Coordination section to dynamically elevate relevant categories based on environmental triggers.

Examples:

| Condition    | Elevated Category    |
| ------------ | -------------------- |
| High UV      | SPF / Sunscreen      |
| Low Humidity | Moisturizers         |
| Dry Weather  | Hydrating Serums     |
| Hot Weather  | Lightweight Products |

### Home Screen

* [ ] Replace mock UV data in the header with live Open-Meteo UV Index values.

---

## 4. Verification

### Network Validation

* [ ] Verify API connectivity.
* [ ] Verify JSON parsing and DTO mapping.

### UI Validation

* [ ] Test High UV scenarios.
* [ ] Test Low Humidity scenarios.
* [ ] Mock multiple environmental conditions to validate UI reactivity.

### Build Verification

* [ ] Run:

```bash
:applications:kocolor:apps:mobile:assembleDebug
```

* [ ] Verify successful build and runtime behavior.

---

## Expected Outcome

Migrating from the current OpenWeatherMap implementation (with mocked UV values) to a privacy-first Open-Meteo architecture will enable KoColor to provide context-aware beauty recommendations automatically.

### Examples

**High UV Day**

* Display "Daily SPF" recommendations.
* Elevate sunscreen products.
* Highlight UV protection routines.

**Dry Climate Day**

* Recommend hydration-focused skincare.
* Elevate moisturizers and hydrating serums.
* Surface moisture-retention beauty tips.

This creates a dynamic beauty experience where environmental conditions directly influence product recommendations, routine guidance, and UI prioritization without requiring user intervention.
