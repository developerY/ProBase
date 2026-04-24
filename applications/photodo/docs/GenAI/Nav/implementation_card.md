# Spending Analytics Visibility Promotion

Promote Spending Analytics by adding a major, high-visibility entry point on the Home screen while maintaining the existing access point on the Transactions screen.

## Proposed Changes

### [Home Feature]

#### [HomeUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/home/src/main/java/com/zoewave/probase/seaweed/mobile/home/ui/HomeUiRoute.kt)

- **New "Analytics Highlight" Card**: Add a prominent card at the top of the Home screen (just below the Hero card) that summarizes recent trends and provides a large, clear button to "Explore Full Analytics".
- **Visual Call-to-Action**: The card will use the `primary` color scheme to draw attention and include an icon (like `Icons.Default.Analytics`) to reinforce its purpose.
- **Dynamic Content**: If possible, show a small snippet of information (e.g., "You've spent X this week, see patterns") to entice the user to click.

### [Transaction Feature]

- **No Changes Required**: The existing `Analytics` icon in the `TransactionsListPane` top app bar will be preserved as requested.

## Verification Plan

### Manual Verification
1.  **Home Screen Check**: Launch the app and confirm the new **"Explore Spending Analytics"** card is highly visible and placed logically within the dashboard.
2.  **Navigation Flow**:
    - Click the new card on the **Home** screen -> Confirm it navigates to the **Spending Analytics** tabbed view.
    - Go to the **Transactions** tab -> Click the analytics icon in the top bar -> Confirm it also navigates to the **Spending Analytics** view.
3.  **Visual Consistency**: Ensure the new card follows the app's design language and doesn't clutter the compact mobile view.
