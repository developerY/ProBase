# Data Deletion Policy for Seaweed
**Developer:** Siamak Ashrafi
**Email:** Developer@ZoeWave.com

At Seaweed, we take your privacy seriously. Because our app is designed to function locally without external accounts, we do not have a centralized database containing your personal information. However, we provide the following pathways for you to manage and delete your data.

## 1. How to Delete Your Personal Content (Local Data)
All of your transactions, bills, categories, budget envelopes, and receipt images are stored entirely on your device. We do not have access to this data, so we cannot delete it for you.

**Steps to delete your content:**
*   **In-App Deletion:** You can delete individual transactions or envelopes directly within the Seaweed app interface.
*   **Complete Removal:** Uninstalling the Seaweed app from your Android device will automatically wipe the local database and all associated app data from your device's internal storage.

## 2. How to Request Deletion of Anonymized Analytics Data
While we do not collect personal identifiers (like names or emails), Seaweed uses Firebase Analytics and Crashlytics to monitor app stability. These services use an anonymous App Instance ID to distinguish your device.

**Steps to request deletion of analytics data:**
1.  Open Seaweed and navigate to **Settings**.
2.  Locate your **App Instance ID** (a unique string of alphanumeric characters).
3.  Email your request to **Developer@ZoeWave.com** with your App Instance ID.

Once received, we will manually trigger a deletion request within the Google Analytics/Firebase console to scrub all records associated with that ID.

## 3. Data Types and Retention Periods

| Data Type | Deletion Status | Retention Policy |
| :--- | :--- | :--- |
| **Financial Data** | User-Controlled | Deleted immediately upon in-app deletion or app uninstallation. |
| **Receipt Images** | User-Controlled | Deleted immediately upon app uninstallation. |
| **Crash Logs** | Permanent Deletion | Scrubbed from Firebase within 90 days of the crash event. |
| **Analytics Identifiers** | By Request | Kept for up to 14 months unless a manual deletion request is submitted. |

## 4. Contact for Privacy Inquiries
If you have any questions regarding your data or this policy, please reach out to us:

*   **Developer:** Siamak Ashrafi
*   **Email:** Developer@ZoeWave.com
