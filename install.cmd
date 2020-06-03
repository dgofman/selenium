@echo off

taskkill /im chromedriver.exe /f
taskkill /im IEDriverServer.exe /f
taskkill /im MicrosoftWebDriver.exe /f 
taskkill /im geckodriver.exe /f
taskkill /im phantomjs.exe /f

CALL mvn clean compile assembly:single

CALL java -jar target/sftselenium-jar-with-dependencies.jar

pause