<p align="center"><img src="https://github.com/BennyKok/PxerStudio/blob/master/app/src/main/ic_launcher-web.png" width="200"></p>
<p align="center"><b>PxerStudio</b> <br>The pixel drawing tool for Android</p>

## About
An open-source pixel drawing tool for Android.

[<img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png"
      alt="Get it on Google Play"
      height="80">](https://play.google.com/store/apps/details?id=com.benny.pxerstudio)
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
      alt="Get it on F-Droid"
      height="80">](https://f-droid.org/packages/com.benny.pxerstudio/)


## Features
- Canvas zooming and moving (two fingers)
- Layer support
- Grid support
- Unlimited undo and redo
- Color picker with an alpha channel
- Various tools (bucket fill, eraser, rectangle, pen, line, eyedropper...)
- Create canvas size up to 128 x 128
- Exportable to GIF, PNG, sprite atlas, folder...
- Support for multiple projects


## Translation
If you want to do translation for this project, please go [here](https://github.com/BennyKok/PxerStudio/blob/master/app/src/main/res/values/strings.xml),
grab the raw file, then copy it to your desktop and replace the string between each XML starting tag and closing tag to the language you want to translate to.
Send it back to me through email, or start a new issue with the file attached and mention the language you are translating.

## Developement

### Gradle Setup

This project is using Gradle Version Catalog to manage dependencies. There are centralized inside the [`libs.versions.toml`](gradle/libs.versions.toml) file, in the `gradle` folder.

### Static Analysis

This project is using [`detekt`](https://github.com/detekt/detekt) to analyze the source code, with the configuration that is stored in the [`detekt.yml`](gradle/detekt.yml) file. It also uses the `detekt-formatting` plugin which includes the `ktlint` rules.

### Continuous Integration

This project is using [GitHub Actions](https://github.com/cortinico/kotlin-android-template/actions).

There are currently the following workflows available:
- [Validate Gradle Wrapper](.github/workflows/gradle-wrapper-validation.yml) - Will check that the gradle wrapper has a valid checksum
- [Check and Lint](.github/workflows/check-and-lint.yml) - Will run `detekt`, `ktlint` and tests
- [Publish Release](.github/workflows/publish-release.yml) - Will publish a new release version of the libraries to Play Store on tag creation

## License
This app is under the [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt) license.
