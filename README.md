# Baker Android App  [![Google Play](http://developer.android.com/images/brand/en_generic_rgb_wo_45.png)](https://play.google.com/store/apps/details?id=com.magloft.demo) [![Build Status](https://travis-ci.org/bakerframework/baker-android-refactor.png)](https://travis-ci.org/bakerframework/baker-android-refactor)

The HTML5 ebook framework to publish interactive books & magazines on any Android device using simply open web standards http://bakerframework.com

Please see the [issues](https://github.com/bakerframework/baker-android-refactor/issues) section to
report any bugs or feature requests and to see the list of known issues.

## Preview

[![View Demo](http://magloft-static.s3.amazonaws.com/baker-android.gif)](http://cdn.magloft.com/preview/baker-android.mp4)

[![Download from Google Play](http://magloft-static.s3.amazonaws.com/baker-screen.png)](https://play.google.com/store/apps/details?id=com.magloft.demo)

## Requirements

1. Android Studio:

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

## Installation
Clone or [Download](https://github.com/bakerframework/baker-android-refactor/archive/4.4.0.zip) this repository, import in Android Studio and build/run the _Baker_ build configuration

    $ git clone git@github.com:bakerframework/baker-android-refactor.git

Beginner-friendly instructions can be found in [Tutorial - Installation Guide](https://github.com/bakerframework/baker-android-refactor/wiki/Tutorial---Installation-Guide)

## Tutorials

Tutorials can be found in the [Baker Android Wiki](https://github.com/bakerframework/baker-android-refactor/wiki)

## Acknowledgements

This project is based on the original [Baker Android](https://github.com/bakerframework/baker-android) project by @fcontreras, @hsalazarl and @nin9creative.

This project follows the features and roadmap of the official [Baker iOS Project](https://github.com/bakerframework/baker), currently maintained by @pieterclaerhout, @nin9creative and the baker community.

This project integrates nicely with [Baker Cloud Console](http://www.bakerframework.com/bakercloudce/) and [MagLoft](http://www.magloft.com).

A special shout-out to Intel Open Source Technology Center for bringing a chromium-based webview to android, allowing modern HTML5/CSS3 awesomeness, which is not provided by the native webview in current android sdks.

It also uses other great open source libraries such as:

* [jsoup](https://github.com/jhy/jsoup)
* [Android Checkout Library](https://github.com/serso/android-checkout)
* [Google GSON](https://code.google.com/p/google-gson)
* [Universal Image Loader](https://github.com/nostra13/Android-Universal-Image-Loader)
* [ViewPagerIndicator](https://github.com/JakeWharton/Android-ViewPagerIndicator)
* [The Crosswalk Project](https://crosswalk-project.org/)

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
