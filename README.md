#NotifyMe

This is a demo application for pick your current location (longitude & Latitude) from your Android device without using GPS (Without On Location Service in the device).

When you on Location Service in the device, It is using more & more battery in your device. Most of Android devices you can use battery for a one day after full charge. In that case, with location service, it will reduce like a half a day or less than that.

## What we are doing here?
In this application we grab the location through GPS and Network Cells. If device's location service is enabled, then we use GPS to get the location. We use the normal Android SDK's method to get the longitude & Latitude. If device's location services are not enabled, then we use our alternative way 'Locations from Network Cells' method to pick relative current location.

## What are these 'Network cells'?
In the globe we all covered from GSM network. Through that we are doing calls, sms, data & etc. If you have a mobile phone, then you also under this network. 

Basically from 3 points we can create area then according to that case, if we think about 3 GSM towers, we can create an area of Network. Simply that area can be call as a Network cell. 

In the globe all of these cells are uniquely named & gives an unique Ids. You guys can get more details about this from [Cellular Network Wiki](https://en.wikipedia.org/wiki/Cellular_network).


## How we pick location from this 'Network Cell'?

If you visit to following [OpenCellID](http://opencellid.org/), you guys also can play with it. If you guys can provide the parameters they request, then you can get the location according to the parameters you provide.

We are working with cell locations, therefore select **Search Location -> cell location** from Left menu. From there you can see, its asking **MCC**, **MNC**, **LAC** & **cell Id** to get the location.

Parameter | Description
--------- | -----------
MCC | Mobile Country Code
MNC | Mobile Network Code
LAC | Location Area Code
Cell ID | Network cell's unique Id

This **MCC** & **MNC** are given for your Network provider. You can get your network provider's those details from [Mobile Country Code Wiki](https://en.wikipedia.org/wiki/Mobile_country_code).

Then this **LAC** & **Cell ID** need to get from your mobile. In Android (Java), you can grab those details from following code snippet.
```java
protected Map<String, Integer> getCellInfo(Context context) {
    GsmCellLocation location;
    Map<String, Integer> cellInfo = new HashMap<String, Integer>();

    TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
    location = (GsmCellLocation) tm.getCellLocation();

    cellInfo.put("cellId", location.getCid());
    cellInfo.put("lac", location.getLac());

    return cellInfo;
}
```

If you have all above parameters then you can try in [OpenCellID](http://opencellid.org/).

## Then how we get longitude & latitude to mobile with above parameters?

If you have above parameters then you have to do a **POST** request to [http://www.google.com/glm/mmap] for get the longitude & latitude. This is done in the code. You can easily find in the code [Connector.java :sendHttpPostReq].
