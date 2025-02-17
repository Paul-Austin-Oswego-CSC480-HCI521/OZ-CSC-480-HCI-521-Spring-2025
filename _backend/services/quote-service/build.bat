@echo off
echo Packaging the service as a WAR...

:: Run Maven build using "call" so control returns to this script.
call mvn clean package
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Build succeeded!
pause
