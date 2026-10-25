# APK

Aquí va el APK de la entrega: `turnos-entrega.apk`.

## Cómo generarlo

En Android Studio: **Build → Generate App Bundles or APKs → Generate APKs**.

O desde la terminal, en la raíz del proyecto:

```bash
./gradlew assembleDebug
cp app/build/outputs/apk/debug/app-debug.apk APK/turnos-entrega.apk
```
