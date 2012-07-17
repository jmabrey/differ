@echo off
set versionName=0.8.9-RELEASE
set targetPath=./target
echo Generating bundle-%versionName%.jar from source files in the %targetPath% directory
jar -cvf bundle-%versionName%.jar %targetPath%/javadjvu-lib-%versionName%.pom %targetPath%/javadjvu-lib-%versionName%.pom.asc %targetPath%/javadjvu-lib-%versionName%.jar %targetPath%/javadjvu-lib-%versionName%.jar.asc %targetPath%/javadjvu-lib-%versionName%-sources.jar %targetPath%/javadjvu-lib-%versionName%-sources.jar.asc %targetPath%/javadjvu-lib-%versionName%-javadoc.jar %targetPath%/javadjvu-lib-%versionName%-javadoc.jar.asc