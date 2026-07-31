@echo off
cd /d "%~dp0"
javac .\src\DodgeGame.java -d .\bin
java -cp .\bin DodgeGame
