#!/bin/bash
# Double-click this file to compile and play the Connect Four game.
# It finds Java automatically and compiles the code for you.

# Move into the folder this file lives in, no matter where it's launched from.
cd "$(dirname "$0")"

echo "============================================"
echo " Connect Four - starting up..."
echo "============================================"
echo ""

# Prefer the locally installed Temurin JDK; fall back to whatever is on PATH.
if [ -x "$HOME/jdks/jdk-21.0.11+10/Contents/Home/bin/java" ]; then
    export JAVA_HOME="$HOME/jdks/jdk-21.0.11+10/Contents/Home"
    export PATH="$JAVA_HOME/bin:$PATH"
fi

# Make sure a real Java compiler is available.
if ! javac -version >/dev/null 2>&1; then
    echo "ERROR: A Java JDK was not found on this computer."
    echo "Install one from https://adoptium.net/ and try again."
    echo ""
    read -p "Press Enter to close this window..."
    exit 1
fi

echo "Compiling the game..."
rm -rf bin && mkdir -p bin
javac -d bin Game_Code/*.java
if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Compilation failed (see the messages above)."
    echo ""
    read -p "Press Enter to close this window..."
    exit 1
fi

echo ""
echo "Opening the Connect Four window..."
echo "(Close the window to finish. Prefer the classic text version?"
echo " Run it any time with:  java -cp bin Client )"
echo ""
java -cp bin ConnectFourGUI

echo ""
echo "Game over. Thanks for playing!"
read -p "Press Enter to close this window..."
