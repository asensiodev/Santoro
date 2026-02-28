# Privacy Policy — Santoro

**Last updated:** March 1, 2026

Ángel Asensio ("we", "us", or "our") built the Santoro app (the "App") as a free application. This page informs you of our policies regarding the collection, use, and disclosure of personal information when you use our App.

By using the App, you agree to the collection and use of information in accordance with this policy.

---

## 1. Information We Collect

### 1.1 Account Information

When you sign in with Google, we collect:

- **Email address**
- **Display name**
- **Profile photo URL**

This information is provided by Google and stored securely via Firebase Authentication. If you choose to continue as a guest, we create an anonymous session and do not collect any personal identifiers.

### 1.2 User-Generated Data

The App stores the following data based on your actions:

- Movies you mark as **Watched** (including the date you watched them)
- Movies you add to your **Watchlist**
- Your **recent search queries** (last 5, stored locally on your device)

This data is stored locally on your device using a local database. If you are signed in with a Google account, your Watched and Watchlist data may be synced to the cloud via Firebase Firestore to enable cross-device access.

### 1.3 Automatically Collected Information

We use third-party services that may collect information used to identify you:

- **Firebase Analytics:** Collects anonymous usage data such as app opens, screen views, and feature usage. This data is aggregated and does not personally identify you.
- **Firebase Crashlytics:** Collects crash reports including device model, OS version, and stack traces to help us fix bugs. These reports do not contain personal information.

### 1.4 Movie Data

The App retrieves movie information (titles, posters, ratings, cast, crew) from **The Movie Database (TMDB) API**. This data is publicly available and is not personal data. We do not send any of your personal information to TMDB.

---

## 2. How We Use Your Information

We use the collected information to:

- **Provide the service:** Authenticate your account, store your movie lists, and sync data across devices.
- **Improve the App:** Analyze anonymous usage patterns and fix crashes.
- **Personalize your experience:** Display your recent searches and movie tracking history.

We do **not** use your information for:

- Advertising or ad targeting
- Selling to third parties
- Profiling or automated decision-making

---

## 3. Data Storage & Security

- **Local data** (movie lists, recent searches) is stored on your device using Android Room database and DataStore. This data remains on your device unless you have enabled cloud sync.
- **Cloud data** (for signed-in users) is stored in Firebase Firestore, hosted by Google. Data is transmitted over encrypted connections (HTTPS/TLS).
- **Authentication** is handled by Firebase Authentication, a Google service that follows industry-standard security practices.

We take reasonable measures to protect your information, but no method of electronic storage or transmission is 100% secure.

---

## 4. Third-Party Services

The App uses the following third-party services, each with their own privacy policies:

| Service | Purpose | Privacy Policy |
|---------|---------|----------------|
| Firebase Authentication | User sign-in and account management | [Google Privacy Policy](https://policies.google.com/privacy) |
| Firebase Firestore | Cloud sync of movie lists | [Google Privacy Policy](https://policies.google.com/privacy) |
| Firebase Analytics | Anonymous usage analytics | [Google Privacy Policy](https://policies.google.com/privacy) |
| Firebase Crashlytics | Crash reporting | [Google Privacy Policy](https://policies.google.com/privacy) |
| TMDB API | Movie data (titles, posters, ratings) | [TMDB Privacy Policy](https://www.themoviedb.org/privacy-policy) |

---

## 5. Data Sharing

We do **not** share, sell, rent, or trade your personal information with third parties.

The only data transmitted outside your device is:

- Account credentials to Firebase (Google) for authentication and cloud sync.
- Anonymous analytics and crash data to Firebase (Google).
- Search queries to TMDB to retrieve movie results (no personal data is included in these requests).

---

## 6. Data Retention

- **Local data** is retained on your device until you uninstall the App or clear its data.
- **Cloud data** (Firestore) is retained as long as your account exists. If you delete your account, your cloud data will be deleted.
- **Analytics data** is retained according to Google's data retention policies (default: 14 months).
- **Crash data** is retained for 90 days in Crashlytics.

---

## 7. Your Rights

You have the right to:

- **Access your data:** Your movie lists are visible in the App at all times.
- **Delete your data:** You can clear your local data by uninstalling the App or clearing app data in Android Settings. To delete your cloud data, you can delete your account from the App or contact us.
- **Opt out of analytics:** You can limit analytics collection by disabling usage data sharing in your device settings.
- **Request account deletion:** Contact us at the email below and we will delete your account and associated data within 30 days.

---

## 8. Children's Privacy

The App is not directed at children under the age of 13. We do not knowingly collect personal information from children under 13. If you are a parent or guardian and you are aware that your child has provided us with personal information, please contact us so we can take the necessary actions.

---

## 9. Changes to This Privacy Policy

We may update our Privacy Policy from time to time. We will notify you of any changes by updating the "Last updated" date at the top of this page. You are advised to review this Privacy Policy periodically for any changes.

---

## 10. Contact Us

If you have any questions or concerns about this Privacy Policy, please contact us at:

**Email:** asensiodev@gmail.com

---

*This product uses the TMDB API but is not endorsed or certified by TMDB.*
