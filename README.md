# AMIV Legiscanner for Checkin (Android)

### Summary 
Android app for scanning legi barcodes and sending to the AMIV checkin server, see [project here](https://gitlab.ethz.ch/amiv/amiv-checkin). 
Used for events, PVK and GV to see if people are registered for the events or to check people in/out of the GV. Note: Several people can use the app simultaneously for the same event.

### How to Install
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
The app has been developed for simplicity and ease of use, everything complex is on the website/server project [here](https://gitlab.ethz.ch/amiv/amiv-checkin). 
The app mainly scans and sends legi numbers to the server and then shows the response.

The app consist of the following activities/screens:
* Main - Login screen for entering an event pin
* Scanning - Barcode scanning and receiving response from server whether a person is registered for the event or not
* Settings - Only for manually setting the server address
