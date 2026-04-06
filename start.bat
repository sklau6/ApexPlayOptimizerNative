@echo off
echo === MBAS Setup ===
echo Adding Node.js to PATH...
set PATH=C:\Users\terence.lau\Downloads\node-v22.13.0-win-x64;%PATH%

echo Installing dependencies...
npm install

echo Starting dev server...
npm run dev
pause
