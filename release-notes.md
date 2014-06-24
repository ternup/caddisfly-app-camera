v0.27
-----
1. Prevent camera preview from closing and opening during multiple analysis
2. Check if phone is placed in a level position on test start (as of now only works if shake is on)
3. Initial delay on start of test increased by a few seconds
4. Prevent invalid entry of rgb value on edit
5. Some minor UI changes (changed: icons, margins, settings summary...)
6. Cloudy settings defaults to true
7. Minimum photo quality defaults to 60%
8. Added alert with exception message on send fail
9. Updated Library : android-async-http 1.4.5
10. Split code into two flavors (Internal (for research purpose) and External (for actual users))

v0.26
-----
1. Multiple sampling for calibration (result will be an average color)
2. Cropping photo to circle before analysis (to match the cartridge shape)
3. Setting : Photo sample dimension, default: 200
4. Setting : Save original photo, default: false (to avoid running out of space)
5. Some UI changes on calibration and result screens
6. Fix: Photo quality check
7. Removed Speedometer UI
8. Removed line chart

v0.25
-----
1. Setting: Sampling Count - number of takes for each analysis (ignored for Bacteria test)
2. Edit option in calibrate screen to enter rgb manually
3. Result rounded to two decimal places instead of one
4. Result of each take displayed in details page
5. Simple countdown display on bacteria test
6. Hindi language
7. Fix: Calibrate screen sometimes crashing
8. Fix: Phone sometimes not sleeping on bacteria test

v0.24
-----
1. Auto click analyze on preview (option in settings)
2. color analysis ignores white, black and gray in the photo
3. Fluoride easy type- just as a test (calibrate 0 and 3 only)
4. fix: Bacteria test fails on phone sleep

v0.23
-----
1. Changed photo sampling dimension 

v0.22
-----
1. Zoom setting ignored (assumption: digital zoom may ruin image). The setting remains in settings for preview purpose
2. Attempt to force focus to center of image (essentially disabling auto focus)
3. Camera shutter sound setting
4. A square highlight on preview screen as a guide for part of the image used for analysis

v0.21
-----
1. Camera Preview added (to fix camera not working on Nexus and MotoG)
2. Removed preset calibration swatches (as each phone camera sees colors differently)
3. Show error message if device has not been calibrated when user starts a test

v0.20
-----
1. Logo changed (Insect wings logo)
2. pH test added

v0.19
-----
1. Bug fixes and stability issues

v0.18
-----
1. Bug fixes and stability issues

v0.17
-----
1. Photo quality range 0 - 100%
2. Analysis count 2 - 100
3. Ignores quality check on bacteria test
4. Images cropped to 600x600 in small folder

v0.16
-----
1. Logo changed
2. Navigation menu changes

v0.15
-----
1. Logo changed
 
v0.14
-----
1. Setting: Minimum photo quality

v0.13
-----
1. Speedometer UI for photo quality on calibration screen

v0.12
-----
1. Photo quality check on calibration and test

v0.11
-----
1. Calibration details screen

v0.10
-----
1. UI enhancements
2. Stability issues fixed

v0.9
-----
1. SQLite DB to hold test and location information
2. Change dashboard UI to first select or add location before starting a test
3. Settings: Cloudy option for camera 
4. Settings: Torch option for camera  
5. Settings: Infinity option for camera (doesn't seem to affect anything)
6. Settings: Zoom setting with preview 
7. Location entry form window
8. Get location via GPS
9. Option to calibrate different test types

v0.8
-----
1. Buttons for different types of test (Fluoride, E.coli, Turbidity, Nitrate, Iron, Arsenic)
2. Language translation
3. Select/Add location before starting a test

v0.7
-----
1. Light and Dark themes
2. Shake device to start a test
3. Alarm sound when shake is required
4. Help screen

v0.6
-----
1. Bacteria interval changed to minutes instead of seconds
2. Camera sound setting

v0.5
-----
1. Location entry dialog

v0.4
-----
1. Navigation drawer menu
2. Select type of test dialog on start test
3. Calibrate Screen

v0.3
-----
1. Bacteria test
2. Test history list
3. Preferences/Settings screen
4. Check update option
5. About Screen
6. Result Details Screen
7. Delete test result

v0.2
-----
1. Simple calibrate option

v0.1
-----
1. Simple Colorimeter
2. Result Screen
3. Swatches List