Implementation Plan - GPS Location and City Name Display
I will integrate the device's GPS to fetch local weather data and display the current city name in the KoColor Home screen.
1. Research & Analysis
   •
   Location Service: Use the existing LocationRepository (which wraps FusedLocationProviderClient) to retrieve coordinates.
   •
   Weather Integration: Update WeatherRepo to ensure coordinate-based weather fetching is fully utilized.
   •
   Dynamic Data: The weather API (api.openweathermap.org) already returns a name field in its response, which corresponds to the city. We will map this to the UI.

2. Technical Steps
   Core & Network Layer
   •
   Ensure WeatherRepo.openCurrentWeatherByCoords is properly implemented in WeatherRepoImpl.
   •
   Verify LocationRepository.updateLocation() handles basic availability checks.
   ViewModel Integration (HomeViewModel)
   •
   [ ] Inject LocationRepository into HomeViewModel.
   •
   [ ] Update HomeUiState to include a locationName: String? field.
   •
   [ ] Refactor fetchWeather():
   ◦
   Call locationRepository.updateLocation().
   ◦
   Collect the first valid LatLng from locationRepository.currentLocation.
   ◦
   Fetch weather using coordinates instead of the hardcoded "Santa Barbara".
   ◦
   Update HomeUiState with the city name from the API response.
   UI Layer (HomeScreen)
   •
   [ ] Update HomeHeader to display the locationName near the weather icon or as part of the greeting.
   •
   [ ] Ensure the weather badge visually associates with the local city name


3. User Review Required
   [!IMPORTANT] This feature requires the Location Permission (ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION). Since KoColor is intended to be privacy-first, I will ensure we only use the location for the immediate weather lookup and do not persist history.
   I've planned the integration of GPS-based weather fetching and city name display. This will replace the hardcoded "Santa Barbara" with your actual current location.
   Should I proceed with these changes?