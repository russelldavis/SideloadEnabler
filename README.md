## SideloadEnabler ##
Enables sideloading of apps on rooted android devices. Many android phones don't need this, but some evil carriers have locked down this setting on their phones.

### Requirements ###
Your phone must be rooted (i.e., you have a working Superuser app)

### Installation ###
Copy SideloadEnabler.apk to your phone using `adb install` or [Sideload Wonder Machine](http://forum.androidcentral.com/android-sideload-wonder-machine/) (it should be the last app you have to manually install).

To test it on the android emulator, see my post about [rooting the android emulator](http://russelldavis.blogspot.com/2011/01/rooting-android-emulator.html).

### How it works ###
On first launch, the app copies itself to the system directory using a shell script run with `su` (this is the hard part -- see the source for details). Once running from that location, the OS gives it extra permissions (in this case, `WRITE_SECURE_SETTINGS`). From there, it's a simple API call to change the setting `install_non_market_apps`.

