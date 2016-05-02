###SFTSelenium

Example of .properties file
```
drivers=Chrome
chrome_driver_path=drivers/chromedriver.exe
load_timeout=30
action_delay=.1
click_delay=3
window_dimension=1900x750
open_fullscreen=false
close_browser=false
```


pom.xml
```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId></groupId>
	<artifactId></artifactId>
	<name></name>
	<version></version>
	<packaging>jar</packaging>
	<url>http://maven.apache.org</url>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
	</properties>

	<repositories>
		<repository>
			<id>dgofman-github</id>
			<url>https://raw.githubusercontent.com/dgofman/selenium/master/</url>
			<snapshots>
				<enabled>true</enabled>
				<updatePolicy>always</updatePolicy>
			</snapshots>
		</repository>
	</repositories>

	<dependencies>
		<dependency>
			<groupId>com.softigent</groupId>
			<artifactId>sftselenium</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>
	</dependencies>
</project>
```

.project
```
<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
	<name>SeleniumTest</name>
	<comment></comment>
	<projects>
	</projects>
	<buildSpec>
		<buildCommand>
			<name>org.eclipse.jdt.core.javabuilder</name>
			<arguments>
			</arguments>
		</buildCommand>
		<buildCommand>
			<name>org.eclipse.m2e.core.maven2Builder</name>
			<arguments>
			</arguments>
		</buildCommand>
	</buildSpec>
	<natures>
		<nature>org.eclipse.jdt.core.javanature</nature>
		<nature>org.eclipse.m2e.core.maven2Nature</nature>
	</natures>
</projectDescription>
```

.classpath
```
<?xml version="1.0" encoding="UTF-8"?>
<classpath>
	<classpathentry kind="con" path="org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER">
		<attributes>
			<attribute name="maven.pomderived" value="true"/>
		</attributes>
	</classpathentry>
	<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
	<classpathentry kind="con" path="org.eclipse.jdt.junit.JUNIT_CONTAINER/4"/>
	<classpathentry kind="src" path="src"/>
	<classpathentry kind="output" path="target/classes"/>
</classpath>
```

log4j.properties

```
## direct log messages to stdout ###
log4j.rootLogger=TRACE, StdoutTrace, StdoutDebug, StdoutInfo, StdoutWarn, StdoutErrorFatal, file
log4j.logger.org.apache.http=ERROR
log4j.logger.com.softigent.sftselenium=TRACE

log4j.appender.StdoutTrace=org.apache.log4j.ConsoleAppender
log4j.appender.StdoutTrace.layout=org.apache.log4j.PatternLayout
log4j.appender.StdoutTrace.layout.conversionPattern=\u001b[32;1m%5p %m\n
log4j.appender.StdoutTrace.threshold=TRACE
log4j.appender.StdoutTrace.filter.filter1=org.apache.log4j.varia.LevelRangeFilter
log4j.appender.StdoutTrace.filter.filter1.levelMin=TRACE
log4j.appender.StdoutTrace.filter.filter1.levelMax=TRACE

log4j.appender.StdoutDebug=org.apache.log4j.ConsoleAppender
log4j.appender.StdoutDebug.layout=org.apache.log4j.PatternLayout
log4j.appender.StdoutDebug.layout.conversionPattern=\u001b[0;36m%5p %m\n
log4j.appender.StdoutDebug.threshold=DEBUG
log4j.appender.StdoutDebug.filter.filter1=org.apache.log4j.varia.LevelRangeFilter
log4j.appender.StdoutDebug.filter.filter1.levelMin=DEBUG
log4j.appender.StdoutDebug.filter.filter1.levelMax=DEBUG

log4j.appender.StdoutInfo=org.apache.log4j.ConsoleAppender
log4j.appender.StdoutInfo.layout=org.apache.log4j.PatternLayout
log4j.appender.StdoutInfo.layout.conversionPattern=\u001B[0;31m%5p %m\n
log4j.appender.StdoutInfo.threshold=INFO
log4j.appender.StdoutInfo.filter.filter1=org.apache.log4j.varia.LevelRangeFilter
log4j.appender.StdoutInfo.filter.filter1.levelMin=INFO
log4j.appender.StdoutInfo.filter.filter1.levelMax=INFO

log4j.appender.StdoutWarn=org.apache.log4j.ConsoleAppender
log4j.appender.StdoutWarn.layout=org.apache.log4j.PatternLayout
log4j.appender.StdoutWarn.layout.conversionPattern=\u001b[0;30;1m%5p %m\n
log4j.appender.StdoutWarn.threshold=WARN
log4j.appender.StdoutWarn.filter.filter1=org.apache.log4j.varia.LevelRangeFilter
log4j.appender.StdoutWarn.filter.filter1.levelMin=WARN
log4j.appender.StdoutWarn.filter.filter1.levelMax=WARN

log4j.appender.StdoutErrorFatal=org.apache.log4j.ConsoleAppender
log4j.appender.StdoutErrorFatal.layout=org.apache.log4j.PatternLayout
log4j.appender.StdoutErrorFatal.layout.conversionPattern=\u001b[31;1m%5p [%C:%L]\n%m\n
log4j.appender.StdoutErrorFatal.threshold=ERROR
log4j.appender.StdoutErrorFatal.filter.filter1=org.apache.log4j.varia.LevelRangeFilter
log4j.appender.StdoutErrorFatal.filter.filter1.levelMin=ERROR
log4j.appender.StdoutErrorFatal.filter.filter1.levelMax=ERROR


log4j.appender.file=org.apache.log4j.RollingFileAppender
log4j.appender.file.maxFileSize=100KB
log4j.appender.file.maxBackupIndex=5
log4j.appender.file.File=./logs/test.log
log4j.appender.file.threshold=debug
log4j.appender.file.layout=org.apache.log4j.PatternLayout
log4j.appender.file.layout.ConversionPattern=%d{yyyy MMM dd HH:mm:ss} %5p (%C) - %m%n
```