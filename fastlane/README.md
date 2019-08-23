fastlane documentation
================
# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```
xcode-select --install
```

Install _fastlane_ using
```
[sudo] gem install fastlane -NV
```
or alternatively using `brew cask install fastlane`

# Available Actions
### test
```
fastlane test
```
Runs all the tests
### deploy
```
fastlane deploy
```
Deploy a new version to the Google Play
  - options:
    - `clean` clean before build if true, default to `true`
    - `offline` let gradle works in offline mode if true, default to `true`
    - `upload` deploy new release to Google Play if true, or just build the aab, default to `true`
  - Example: `fastlane deploy clean:false offline:false upload:false`


----

This README.md is auto-generated and will be re-generated every time [fastlane](https://fastlane.tools) is run.
More information about fastlane can be found on [fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
