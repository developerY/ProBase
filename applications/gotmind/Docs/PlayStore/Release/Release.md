Based on the Google Play Console screenshot in `image_26d940.png`, you are running into two closely related errors preventing you from rolling out to Open Testing.

Here is exactly what is happening and how to fix it:

### The Diagnosis

The errors indicate that **your release draft is currently empty**.

1. **"This release does not add or remove any app bundles"**: You haven't attached a new Android App Bundle (.aab) to this specific release, or it was accidentally removed before you hit the "Preview and confirm" stage.
2. **"You can't rollout this release because it doesn't allow any existing users to upgrade..."**: Because there is no new app bundle attached (or if there is, the `versionCode` is the same or lower than an existing release in a lower track like Internal Testing), the Play Store sees no valid upgrade path for your users.

### How to Fix It

To resolve this and successfully launch your open testing track, follow these steps:

1. **Navigate Back:** At the top of the screen (under "Create open testing release"), click on the **1 - Create release** step to go back to the upload page, or click **Discard draft release** on the right to start completely fresh.
2. **Increment Your Version Code:** Before uploading, ensure your new build has a higher `versionCode` than any previous upload (Internal, Closed, or Production). Since you are working with AGP 9.0, double-check that your `versionCode` and `versionName` are correctly incremented in your app-level `build.gradle.kts` file.
3. **Upload the Bundle:** In the "App bundles" section of the "Create release" page, upload your generated `.aab` file.
4. **Save and Preview:** Once the upload finishes processing and appears in the list of added bundles, click **Save** and then **Review release** (or Next) to return to the preview screen.

Once the new bundle with a higher version code is successfully attached, those errors will clear out, and the gray "Save" button in the bottom right will change to a blue "Start rollout to Open testing" button.