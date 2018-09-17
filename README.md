# amiv Check-in App (Android)

## Notice
This app is now part of the generic [amiv Android app](https://gitlab.ethz.ch/amiv/amiv-app-android) and has its own branch and java package.
To continue editing the android version of the check-in app please do so there, in the checkin branch. Please note that it has its own readme there.

### Summary 
Android app for scanning legi barcodes and sending to the AMIV checkin server, see [project here](https://gitlab.ethz.ch/amiv/amiv-checkin). 
Used for events, PVK and GV to see if people are registered for the events or to check people in/out of the GV. Note: Several people can use the app simultaneously for the same event.

### How to Install
**Download at the Google Play Store: https://play.google.com/store/apps/details?id=ch.amiv.checkin**

Alternatively:
1. Download using the _download link in bold (not the download icon or download zip!)_ in most recent tag [here](https://gitlab.ethz.ch/amiv/amiv-checkin-app/tags). Alternatively, view the "Legiscanner Builds\Release Build dd-mm-yy\release" Folder in the source and download the .apk file.
2. Open the .apk file on your Android device
3. Note: You need to temporarily allow installations from unknown sources, there should be a pop-up about this. You should turn off this setting after the installation is complete

### How to Use the App
1. Get a valid pin from your event organiser and enter it into the pin field.
2. If you get an invalid URL error then close the keyboard and press the settings button at the bottom to change to URL.
3. Ensure you allow the app to use your camera, or the barcode scanning will not work.
4. Once you have submitted a valid pin, you will be able to enter legi numbers manually, or scan them from a legi.
5. The barcodes will be scanned as soon as the barcode is in focus.

### Structure (for developers)
For bugs, create an issue report. For questions regarding the app (not website or server-side) contact developer: rbarton@ethz.ch
The app has been developed for simplicity and ease of use, everything complex is on the website/server project [here](https://gitlab.ethz.ch/amiv/amiv-checkin). 
The app mainly scans and sends legi numbers to the server and then shows the response.

The app consist of the following activities/screens:
* Main - Login screen for entering an event pin
* Scanning - Barcode scanning and receiving response from server whether a person is registered for the event or not
* Settings - Only for manually setting the server address
* Member List - Used to display the data: stats, event info and members. Like the checkin website.

Also note the static ServerRequests, which is used to handle most (not all) requests for data from the server. The EventDatabase is also central, where all the data is stored as the name would suggest, use the static instance to access it.

When updating store page content use the guidelines in: https://github.com/Triple-T/gradle-play-publisher#upload-images
Also note to increment the version code.
The test, build and deploy has been setup with Gitlab CI, which will test and build automatically for any new commit. You can then execute the deploy stage by going to the CI/CD>Pipelines page on the gitlab website.