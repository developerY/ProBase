# Add Subscription Management to Seaweed

Implement a comprehensive subscription management feature in `seaweed` to help users track and manage their recurring expenses. This includes data persistence, a dedicated mobile interface with adaptive layouts, and a WearOS companion for quick status checks.

## User Review Required

- [ ] **Subscription Frequency**: Currently planning support for Monthly and Yearly. Are there other frequencies (e.g., Weekly, Quarterly) that should be prioritized?
- [ ] **Integration with Transactions**: Should paying a subscription automatically create a transaction in the `transactions` table? (Planning to keep them separate for now but allow manual conversion).

## Proposed Changes

### [Data] Subscription Entity & Persistence
Expand the seaweed data layer to support subscriptions.

#### [NEW] [Subscription.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/model/src/main/java/com/zoewave/probase/seaweed/model/Subscription.kt)
- Define `Subscription` domain model: `id`, `name`, `amount`, `frequency`, `nextBillingDate`, `category`.

#### [NEW] [SubscriptionEntity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/database/src/main/java/com/zoewave/probase/seaweed/database/SubscriptionEntity.kt)
- Define Room entity and mappers.

#### [NEW] [SubscriptionDao.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/database/src/main/java/com/zoewave/probase/seaweed/database/SubscriptionDao.kt)
- Add CRUD operations for subscriptions.

#### [SeaweedDatabase.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/database/src/main/java/com/zoewave/probase/seaweed/database/SeaweedDatabase.kt)
- Register `SubscriptionEntity` and `SubscriptionDao`.

#### [NEW] [SubscriptionRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/data/src/main/java/com/zoewave/probase/seaweed/data/SubscriptionRepository.kt)
- Define repository interface.

#### [NEW] [SubscriptionRepositoryImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/data/src/main/java/com/zoewave/probase/seaweed/data/SubscriptionRepositoryImpl.kt)
- Implement repository using DAO.

---

### [Mobile] Subscription Feature Module
Create a new feature module for mobile subscription management.

#### [NEW] [:applications:seaweed:apps:mobile:features:subscriptions](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/subscriptions)
- Implement Subscription List, Detail, and Add/Edit screens.
- Use `AdaptiveSeaweedScreen` pattern for tablet/foldable support.

#### [SeaweedDestination.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/model/src/main/java/com/zoewave/probase/seaweed/model/navigation/SeaweedDestination.kt)
- Add `Subscriptions` route.

#### [SeaweedBottomBar.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/src/main/java/com/zoewave/probase/seaweed/mobile/ui/components/SeaweedBottomBar.kt)
- Add Subscriptions tab.

---

### [WearOS] Subscription Feature Module
Create a new feature module for WearOS subscription overview.

#### [NEW] [:applications:seaweed:apps:wear:features:subscriptions](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/wear/features/subscriptions)
- Implement a glanceable subscription list for WearOS.

## Verification Plan

### Automated Tests
- `gradlew applications:seaweed:database:test` (Add unit tests for DAO)
- `gradlew applications:seaweed:apps:mobile:assembleDebug`
- `gradlew applications:seaweed:apps:wear:assembleDebug`

### Manual Verification
- Add a new subscription on mobile and verify it appears in the list.
- Check the WearOS app to see the list of upcoming subscription renewals.
- Verify adaptive layout on a tablet emulator for the Subscriptions screen.
