# ICONex
> ICONex Android version

ICONex is a wallet where you can keep not only ICX but also other various crypto-currencies.


## Dependencies
* [OkHttp](http://square.github.io/okhttp/) for Http connections
* [Realm](https://realm.io/) for database
* [Retrofit](http://square.github.io/retrofit/) for Http API
* [SLF4J](https://www.slf4j.org/) for Web3j
* [Spongy Castle](https://rtyley.github.io/spongycastle/) for crypto
* [Web3j](https://github.com/web3j/web3j) for Ethereum network
* [ZXing](https://github.com/zxing/zxing) for generate QR Code

## Requirement
* Android Studio

## Open in Android Studio
Clone repository
``` sh
git clone https://github.com/icon-project/iconex_android.git
```
Finally open the iconex_android directory in Android Studio

## Developer mode
ICONex for Android supports developer mode since version 1.5.0

### How to
* Require 3rd party app or Web pages
* Using communication protocol [ICONex connect](https://github.com/icon-project/iconex_android/tree/develop/docs/Connect)
* Just set action "DEVELOPER"
``` Java
Intent intent = new Intent()
                .setClassName("foundation.icon.iconex", "foundation.icon.connect.ConnectReceiver")
                .setAction("DEVELOPER")
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

sendBroadcast(intent);
```
* When activated, users can choose ICON Service provider via App Info menu.
* [ICON Network](https://github.com/icon-project/icon-project.github.io/blob/master/docs/icon_network.md)
* Note: ** It won't be changed until user deactivates mode. **

## Download
* Google play - [ICONex](https://play.google.com/store/apps/details?id=foundation.icon.iconex)

## Experimental
* This project contains lots of experimental elements.
