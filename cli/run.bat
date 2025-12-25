@echo off
REM Changelog Hub CLI Launcher
REM Usage: run.bat [command] [options]

set SCRIPT_DIR=%~dp0
set JAR_FILE=%SCRIPT_DIR%target\cli-1.0.0-SNAPSHOT.jar

if not exist "%JAR_FILE%" (
    echo JAR file not found: %JAR_FILE%
    echo Please build the project first: mvn clean package -pl cli -am
    exit /b 1
)

java -jar "%JAR_FILE%" %*
