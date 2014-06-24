from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice
import commands
import sys
import os

# starting the application and test
print "Starting the monkeyrunner script"

if not os.path.exists("c:/Projects/screenshots"):
    print "creating the screenshots directory"
    os.makedirs("c:/Projects/screenshots")

# connection to the current device, and return a MonkeyDevice object
device = MonkeyRunner.waitForConnection()

device.installPackage('C:/Projects/Caddisfly/caddisfly-sensor-android/Caddisfly/build/apk/release.apk')

package = 'com.ternup.caddisfly'
activity = 'com.ternup.caddisfly.activities.MainActivity'
runComponent = package + '/' + activity


print "starting application...."
device.startActivity(component=runComponent)

#screenshot
MonkeyRunner.sleep(1)
result = device.takeSnapshot()
result.writeToFile('c:/Projects/screenshots/splash.png','png')
print "screenshot taken and stored on device"

#sending an event which simulate a click on the menu button
device.press('KEYCODE_MENU', MonkeyDevice.DOWN_AND_UP)

print "Finishing the test" 