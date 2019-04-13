@echo off

REM enable ANSI color in DOS
REG ADD HKCU\Console /v VirtualTerminalLevel /t REG_DWORD /d 00000001 -f

mkdir build

cd build

REM Disable Git SSL verification in the server hosting
git config --global http.sslVerify false

REM download latest changed from GIT
git clone https://github.com/dgofman/selenium.git

REM goto inside downloaded git directory
cd selenium

REM get latest changes from GIT
git fetch origin
git merge origin/master

REM build com.softigent:sftselenium:jar
call mvn install

REM goto back to project directory
cd ../../..

call mvn install

pause