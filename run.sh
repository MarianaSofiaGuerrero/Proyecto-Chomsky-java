#!/usr/bin/env bash
# Compila y ejecuta el simulador (Linux / macOS). Requiere JDK 11 o superior.
set -e
cd "$(dirname "$0")"
rm -rf out && mkdir -p out
javac -encoding UTF-8 -d out $(find src/main/java -name '*.java')
java -cp out chomsky.Main
