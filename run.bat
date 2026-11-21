@echo off
REM Compila y ejecuta el simulador (Windows). Requiere JDK 11 o superior.
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
dir /s /b src\main\java\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
del sources.txt
java -cp out chomsky.Main
