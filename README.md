# Baker Android App  [![Google Play](http://developer.android.com/images/brand/en_generic_rgb_wo_45.png)](https://play.google.com/store/apps/details?id=com.magloft.demo) [![Build Status](https://travis-ci.org/bakerframework/baker-android-refactor.png)](https://travis-ci.org/bakerframework/baker-android-refactor)

The HTML5 ebook framework to publish interactive books & magazines on any Android device using simply open web standards http://bakerframework.com

[![Download from Google Play](http://magloft-static.s3.amazonaws.com/baker-screen.png)](https://play.google.com/store/apps/details?id=com.magloft.demo)


Please see the [issues](https://github.com/bakerframework/baker-android-refactor/issues) section to
report any bugs or feature requests and to see the list of known issues.

## Installation

1. Install [Github Client](https://mac.github.com/) or [git](http://git-scm.com/book/en/v2/Getting-Started-Installing-Git)
2. Download and install [Android Studio](http://developer.android.com/sdk/index.html)
3. Open Android SDK Manager and install the following dependencies:
  
        Tools > Android SDK Tools
        Tools > Android SDK Platform-tools
        Tools > Android SDK Build-tools
        Android 5.0.1 (API 21) > SDK Platform
        Extras > Android Support Repository
        Extras > Android Support Library
        Extras > Google Play services
        Extras > Google Repository
        Extras > Google Play Billing Library
        Extras > Google Play Licensing Library


4. Open [Github Client](https://mac.github.com/) and clone the [baker-android-refactor repository](https://github.com/bakerframework/baker-android-refactor)
5. Open [Android Studio](http://developer.android.com/sdk/index.html) and choose "File" > "Import Project"
6. Choose the directory where the baker-android-refactor repository is located.
7. Choose "Build" > "Make Project"
8. Choose "Run" > "Run Baker"
9. In the "Choose Device" dialog, either select "Choose a running device" when you have an android device attached via USB, or select "Launch Emulator" (you need to initially create a virtual device)

## Tutorials

Tutorials can be found in the [Baker Android Wiki](https://github.com/bakerframework/baker-android-refactor/wiki)

## Acknowledgements

This project is based on the original [Baker Android](https://github.com/bakerframework/baker-android) project by @fcontreras, @hsalazarl and @nin9creative.

This project follows the features and roadmap of the official [Baker iOS Project](https://github.com/bakerframework/baker), currently maintained by @pieterclaerhout, @nin9creative and the baker community.

This project integrates nicely with [Baker Cloud Console](http://www.bakerframework.com/bakercloudce/) and [MagLoft](http://www.magloft.com).

It also uses other great open source libraries such as:

* [jsoup](https://github.com/jhy/jsoup)
* [Android Checkout Library](https://github.com/serso/android-checkout)
* [Google GSON](https://code.google.com/p/google-gson)
* [Universal Image Loader](https://github.com/nostra13/Android-Universal-Image-Loader)
* [ViewPagerIndicator](https://github.com/JakeWharton/Android-ViewPagerIndicator)

## Contributing

Please fork this repository and contribute back using
[pull requests](https://github.com/bakerframework/baker-android-refactor/pulls).

Any kind of contribution, features, bug fixes, language translations, unit/integration tests, refactors are welcome and appreciated but will be thoroughly reviewed and discussed.

Please make sure that:

* Your pull request is based on the latest possible version of the Baker Android `master` branch
* You have provided a meaningful title for your pull request
* You have briefly explained in the pull request what it does and why it should be merged
* You have referenced any relevant issue

## License

This software is free to use under the BSD license.
See the [LICENSE file][] for license text and copyright information.


[LICENSE file]: https://github.com/bakerframework/baker-android-refactor/blob/master/LICENSE.md
