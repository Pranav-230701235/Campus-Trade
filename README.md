# 🎓 Campus Trade REC

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)

**Campus Trade REC** is a dedicated marketplace Android application built exclusively for the **Rajalakshmi Engineering College (REC)** community. It enables students to securely buy, sell, and request items right on campus, fostering a sustainable and helpful student ecosystem.

---

## ✨ Key Features

- 🔐 **Secure College Authentication:** Login is strictly restricted to valid REC college email domains, ensuring a safe environment.
- 🛒 **Marketplace Listings:** Browse, search, and view detailed product listings uploaded by peers.
- 📝 **Real-time Requests Wall:** Looking for something specific? Post a request on the wall and let others know what you need!
- 🛍️ **Seamless Ordering & Wishlist:** Add items to your wishlist, confirm orders easily, and track your purchases natively in the app.
- 🔔 **Push Notifications (FCM):** Receive automated, real-time alerts for new student requests or order updates.
- 🖼️ **Image Management:** Smooth and fast image loading using Glide.
- ⚙️ **Profile & Settings:** Personalized profiles with customizable settings and an overview of your active listings and purchases.

---

## 🛠️ Tech Stack

- **Language:** Kotlin
- **Architecture & UI:** ViewBinding, Material Design Components
- **Backend/BaaS:** Firebase (Authentication, Firestore)
- **Push Notifications:** Firebase Cloud Messaging (FCM)
- **Libraries:** OkHttp, Glide

---

## 🚀 Getting Started

### Prerequisites
- Android Studio
- Minimum SDK: API 24
- Target SDK: API 36
- A Firebase Project with Authentication, Firestore, and Cloud Messaging enabled.

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Pranav-230701235/Campus-Trade.git
   cd Campus-Trade
   ```

2. **Open the project:**
   Open Android Studio and select `Open an existing Android Studio project`, then navigate to the cloned directory.

3. **Configure Firebase:**
   - Go to the [Firebase Console](https://console.firebase.google.com/).
   - Add a new Android app to your project with the package name `com.example.collegemarketplace`.
   - Download the `google-services.json` file.
   - Place the `google-services.json` file into the `app/` directory of the project.

4. **Build and Run:**
   - Sync the project with Gradle files.
   - Build and run the app on an Android emulator or a physical device.

---

