# Task Management

- [/] Research and Setup
	- [x] Explore GoSwift project structure
	- [x] Explore core:data HealthConnect implementation
	- [ ] Identify required Health Connect data types for Exercise and Sleep
- [ ] Core Data Enhancements
	- [ ] Update `HealthConnectRepository` to support Sleep data
	- [ ] Update `HealthConnectRepositoryImpl` with Sleep data reading
- [ ] GoSwift Data Layer
	- [ ] Add `:core:data` dependency to `goswift:data`
	- [ ] Create `HealthRepository` in `goswift:data`
- [ ] GoSwift Domain/ViewModel Layer
	- [ ] Update `HomeViewModel` to fetch sleep and exercise data
	- [ ] Implement correlation logic (caffeine vs sleep/exercise)
- [ ] UI Updates
	- [ ] Display sleep and exercise data in Home screen
	- [ ] Update recommendations based on health data
