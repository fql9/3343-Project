@echo off
cd /d "%~dp0"
java --module-path lib --add-modules javafx.controls,javafx.fxml -jar lib/SecondHandTrading-1.0-all.jar
pause
