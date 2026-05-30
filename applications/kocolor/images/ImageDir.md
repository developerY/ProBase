The short answer is: **No, those images will not be bundled into your APK.** You are completely safe leaving that directory exactly where it is. Here is the technical breakdown of why your APK size is protected and how Android Studio handles that folder.

### 1. The Gradle Boundary

When you build an Android app, the Android Gradle Plugin (AGP) is highly restrictive. It only packages files that live inside explicitly defined source sets within the app module itself. To make it into the APK, an image must be placed inside a designated folder like:
`kocolor/apps/mobile/src/main/res/drawable/`
or
`kocolor/apps/mobile/src/main/assets/`

Because your `kocolor/images/` directory sits way up at the module root—completely outside the `src` path of the `mobile` app—the Gradle compiler turns a blind eye to it. It will not be compiled, packaged, or shipped to the Google Play Store.

### 2. What "Excluded" Actually Means

You mentioned the folder is marked as excluded. In Android Studio (IntelliJ), marking a directory as "Excluded" is an IDE-level optimization, not a build instruction.

* It tells the IDE's background indexer: *"Do not scan this folder for code, XML, or searchable text."* * This saves your Mac's RAM and CPU, keeping Android Studio fast and snappy. It has no bearing on the APK build itself, but it is the exact right move for a folder full of media files.

### Should you move it out of the KoColor project?

**No, keeping it there is actually a great architectural practice.** Having an `images` or `design_assets` folder at the root of your repository is standard for clean architecture. It allows you to:

* Keep all your raw, high-res, or master WebP files strictly version-controlled alongside your code.
* Serve as a "staging area" for your assets.
* Easily collaborate without bloating the actual mobile app module.

**The Workflow:**
Leave your master repository of images in `kocolor/images/`. When you are ready to actually display one of those 14KB optimized WebP images in your Jetpack Compose UI, simply copy that *single* file into `kocolor/apps/mobile/src/main/res/drawable/`.

Your project structure is perfectly optimized for a lean APK!