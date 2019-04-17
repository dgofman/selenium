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
sudo yum install firefox