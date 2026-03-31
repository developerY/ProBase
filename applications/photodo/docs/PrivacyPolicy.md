# Privacy Policy for PhotoDo

**Effective Date:** [Insert Date, e.g., March 29, 2026]

**[Your Name or Company Name]** ("we", "us", or "our") built the PhotoDo app as a [Free / Freemium / Ad-Supported / Commercial] app. This SERVICE is provided by **[Your Name or Company Name]** at no cost and is intended for use as is.

This page is used to inform visitors regarding our policies with the collection, use, and disclosure of Personal Information if anyone decided to use our Service.

### 1. Information Collection and Use
PhotoDo is designed with your privacy in mind. **We do not collect, transmit, distribute, or sell your personal data.** All data created within the app—including your project names, tasks, financial budgets, expenses, and photos—is stored entirely locally on your device's internal storage using a local database. We do not have access to your data, and it is not synced to any external servers by us.

### 2. Camera and Device Storage Permissions
To provide the core functionality of the app, PhotoDo requires specific device permissions:
* **Camera Permission:** We request access to your device's camera solely to allow you to take "Context Photos" for your projects and tasks.
* **Storage Access:** Photos taken within the app are saved directly to your device's local storage.

We do not upload, view, or analyze these photos. They remain strictly on your device and are only accessible by you.

### 3. Third-Party Services
That is a very smart move for launch day. Even though your app saves everything locally to the device's database, you as the developer still need to know if the app is actually working out in the wild.

Here is the easiest way to think about the difference between the two: **Analytics tracks *behavior*, and Crashlytics tracks *stability*.**

### 1. Firebase Analytics (The "What are users doing?" tool)
Analytics tells you how people are interacting with your app. It collects anonymized usage data so you can see if your design is successful.
* **What it tracks:** Screen views, button clicks, user retention, and custom events.
* **PhotoDo Example:** You can log an event every time someone clicks your new `QuickExpenseBar` vs. opening the full "Add Expense" dialog. If 90% of users use the Quick Bar, you know that was a highly successful feature! It also tells you basic demographics (e.g., 80% of your users are on Android 14).

### 2. Firebase Crashlytics (The "Is the app broken?" tool)
Crashlytics is strictly for finding and fixing bugs. If PhotoDo crashes on a user's phone, they usually just uninstall it without telling you. Crashlytics acts as your automated mechanic.
* **What it tracks:** Fatal crashes, non-fatal errors, and the exact line of Kotlin code that caused the app to die.
* **PhotoDo Example:** If a user tries to attach a massive 50MB photo to a project and the app runs out of memory and crashes, Crashlytics will immediately send you a report saying: *"Crash at TaskDetailScreen.kt line 142 on a Samsung Galaxy S23."* This allows you to push a fix before bad reviews roll in.


### 3. Third-Party Services
While the core data of your projects and finances remains securely stored on your local device, the app does use third-party services that may collect **anonymized information** used to identify your device, **analyze app usage**, and **improve stability**.
Link to privacy policy of third-party service providers used by the app:
* [Google Play Services](https://policies.google.com/privacy)
* [Google Analytics for Firebase](https://firebase.google.com/policies/analytics)
* [Firebase Crashlytics](https://firebase.google.com/support/privacy/)

### 4. Security
We value your trust in using our app. Because your data is stored locally, its security relies on the security of your device. We recommend utilizing standard device security features, such as a passcode or biometric lock, to protect your device and the data within PhotoDo.

### 5. Children’s Privacy
These Services do not address anyone under the age of 13. We do not knowingly collect personally identifiable information from children under 13. Because the app operates offline and locally, no data is transmitted to us from users of any age.

### 6. Changes to This Privacy Policy
We may update our Privacy Policy from time to time. Thus, you are advised to review this page periodically for any changes. We will notify you of any changes by posting the new Privacy Policy on this page.

### 7. Contact Us
If you have any questions or suggestions about our Privacy Policy, do not hesitate to contact us at **Developer@ZoeWave.com**.