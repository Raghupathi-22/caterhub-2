# CaterHub Android Project — Recovered

This is the recovered Android project built from the uploaded `app.zip`.

The uploaded archive contained the Android app module but was missing `app/build.gradle.kts` and root Gradle configuration. Those configuration files were restored from the current public `main-UI-screen` branch of the CaterHub repository.

The generated `app/build/` directory was intentionally excluded because it is disposable build output.

Open this folder in Android Studio and allow Gradle to sync.

Known limitation from the uploaded source:
The existing unit-test files include stale tests that previously caused `compileDebugUnitTestKotlin` failures. The production Android source itself had reached `assembleDebug` in the supplied build log. Do not treat the generated build directory from the old archive as source code.
