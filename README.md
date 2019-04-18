###SFTSelenium

IE Drivers: http://selenium-release.storage.googleapis.com/index.html
Chrome Drivers: https://sites.google.com/a/chromium.org/chromedriver/downloads
Firefox Drivers: https://github.com/mozilla/geckodriver/releases

### Install Chrome Engine on Red Hat
cat /proc/version
https://chromium.woolyss.com/
wget https://dl.google.com/linux/direct/google-chrome-stable_current_x86_64.rpm
yum install ./google-chrome-stable_current_*.rpm -y
rm google-chrome-stable_current_*.rpm
export CHROME_PATH=/usr/bin/google-chrome


### Install FireFox Engine on Red Hat
wget -O- "https://download.mozilla.org/?product=firefox-latest-ssl&os=linux64&lang=en-US" | sudo tar -jx -C /usr/local/
sudo ln -s /usr/local/firefox/firefox /usr/bin/firefox
or
sudo yum install Xvfb firefox
sudo Xvfb :10 -ac &
sudo export DISPLAY=:10

##"Failed to open connection to "session" message bus: Unable to autolaunch a dbus-daemon without a $DISPLAY for X11"
sudo yum install dbus-x11
export $(dbus-launch)
# TEST: dbus-send --session --print-reply --dest="org.freedesktop.DBus" /org/freedesktop/DBus  org.freedesktop.DBus.ListNames

##Gtk-WARNING **: Locale not supported by C library.
export LC_ALL="en_US"
Optional:
export LANG="en_US"
export LANGUAGE="en_NZ"
export C_CTYPE="en_US"
export LC_NUMERIC=
export LC_TIME=en"en_US"

