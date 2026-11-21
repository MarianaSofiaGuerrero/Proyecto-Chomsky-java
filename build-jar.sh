#!/usr/bin/env bash
# Genera dist/chomsky-simulator.jar (ejecutable con doble clic o java -jar).
set -e
cd "$(dirname "$0")"
rm -rf out && mkdir -p out dist
javac --release 11 -encoding UTF-8 -d out $(find src/main/java -name '*.java')
jar --create --file dist/chomsky-simulator.jar --main-class chomsky.Main -C out .
echo "Listo: dist/chomsky-simulator.jar"
