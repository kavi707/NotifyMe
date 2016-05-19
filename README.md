#NotifyMe

This is a demo application for pick your current location (longitude & Latitude) from your Android device without using GPS (Without On Location Service in the device).

When you on Location Service in the device, It is using more & more battery in your device. Most of Android devices you can use battery for a one day after full charge. In that case, with location service, it will reduce like a half a day or less than that.

## What we are doing here?
In this application we grab the location through GPS and Network Cells. If device's location service is enabled, then we use GPS to get the location. We use the normal Android SDK's method to get the longitude & Latitude. If device's location services are not enabled, then we use our alternative way 'Locations from Network Cells' method to pick relative current location.
