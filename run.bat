@echo off
cd /d "%~dp0"
javac .\src\*.java -d .\bin
java -cp .\bin JPanal
