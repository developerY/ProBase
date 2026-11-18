The `WearableListenerService` is the exact Android component designed to be the invisible catcher's mitt for your data. It is part of the Google Play Services Wearable Data Layer API, and it is built specifically to solve the "syncing" problem between phones and watches.

Here is exactly how it fits into your architecture:

### The Two Halves of the Bridge

**1. The Sender (The Watch)**
When a user finishes a ride on the watch, your `BikeForegroundService` saves the data to the watch's local Room database. Right after that, the watch uses the **`DataClient`** to bundle up that `BikeRideEntity` (and all its `LocationPoint`s) and tosses it over the Bluetooth connection.
*(Note: We use `DataClient` instead of `MessageClient` here because a 2-hour bike ride with thousands of GPS coordinates is usually too large for a simple message ping).*

**2. The Receiver (The Phone's `WearableListenerService`)**
This service is registered in your phone's `AndroidManifest.xml`. The beauty of it is that your phone app doesn't even need to be open. The moment the watch's data arrives via Bluetooth or Wi-Fi, the Android OS automatically wakes up your `WearableListenerService` in the background.
It unpacks the data, injects your `BikeRideRepo`, and inserts the ride directly into the phone's database. The next time the user opens the app, the ride is magically sitting in their History tab.

### The Implementation Plan

To build this bridge, we need to write two things:

1. **The Watch Side:** A quick function to serialize your ride data and push it to the `DataClient`.
2. **The Phone Side:** The `WearableListenerService` that catches it and saves it to Room.

Would you like to start by writing the `WearableListenerService` for the phone so it is ready and waiting to catch the data, or should we write the watch's sending logic first?