@echo off
REM Genera dist\chomsky-simulator.jar (Windows).
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
if not exist dist mkdir dist
dir /s /b src\main\java\*.java > sources.txt
javac --release 11 -encoding UTF-8 -d out @sources.txt
del sources.txt
jar --create --file dist\chomsky-simulator.jar --main-class chomsky.Main -C out .
echo Listo: dist\chomsky-simulator.jar
