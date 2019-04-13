#!/bin/bash

mkdir build

cd build

#Disable Git SSL verification in the server hosting
git config --global http.sslVerify false

#download latest changed from GIT
git clone https://github.com/dgofman/selenium.git

#goto inside downloaded git directory
cd selenium

#get latest changes from GIT
git fetch origin
git merge origin/master

#build com.softigent:sftselenium:jar
mvn install

#goto back to project directory
cd ../../..

mvn install