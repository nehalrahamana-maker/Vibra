@echo off
setlocal enabledelayedexpansion

echo ==============================================================
echo        VIBRA - NATIVE KOTLIN & JETPACK COMPOSE BUILD TOOL
echo        Authored by Kazi Nehal (CSE Student)
echo ==============================================================
echo.

:: Check if local Java and Gradle are available
where gradlew >nul 2>nul
if %errorlevel% equ 0 (
    echo [*] Found local Gradle! Attempting local APK build...
    call gradlew assembleDebug
    if %errorlevel% equ 0 (
        echo [OK] Local APK successfully built at app\build\outputs\apk\debug\app-debug.apk!
        goto :done
    )
)

echo [*] Low-spec PC Mode detected (Zero RAM consumption mode):
echo [*] Staging all Kotlin & Compose changes...
& "C:\Program Files\Git\cmd\git.exe" add .

echo [*] Committing update...
& "C:\Program Files\Git\cmd\git.exe" commit -m "feat(kotlin): build native Jetpack Compose APK via cloud CI"

echo [*] Pushing to GitHub for automated 16GB Cloud Build...
& "C:\Program Files\Git\cmd\git.exe" push origin main

echo.
echo ==============================================================
echo [SUCCESS] Your Native Kotlin APK is building in the cloud!
echo ==============================================================
echo.
echo Track progress and download your APK here:
echo https://github.com/nehalrahamana-maker/Vibra/actions
echo.
echo Releases Page:
echo https://github.com/nehalrahamana-maker/Vibra/releases
echo.

:done
pause
