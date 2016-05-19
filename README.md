#NotifyMe

This is a demo application for pick your current location (longitude & Latitude) from your Android device without using GPS (Without On Location Service in the device).

When you on Location Service in the device, It is using more & more battery in your device. Most of Android devices you can use battery for a one day after full charge. In that case, with location service, it will reduce like a half a day or less than that.

## What we are doing here?
In this application we grab the location through GPS and Network Cells. If device's location service is enabled, then we use GPS to get the location. We use the normal Android SDK's method to get the longitude & Latitude. If device's location services are not enabled, then we use our alternative way 'Locations from Network Cells' method to pick relative current location.

## What is this Network cells and how we grap location from those?
In the globe all we covered from GSM network. Through that all we doing calls, sms, data & etc. If you have a mobile phone, then you also under this network. Basically from 3 points we can create area. Then from according to that case, if we think about 3 GSM towers, we can create an area of Network. Simply that area can be call as a Network cell. In the globe all of these cells are uniquely named & gives an unique Ids. You guys can get more details about this from [Cellular Network Wiki](https://en.wikipedia.org/wiki/Cellular_network).
