@echo off
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0INICIAR.ps1"
set "RESULTADO=%ERRORLEVEL%"
echo.
if not "%RESULTADO%"=="0" (
  echo El arranque encontro un error. Revisa el mensaje anterior.
) else (
  echo La API esta lista. Puedes ejecutar PROBAR_API.ps1.
)
pause
exit /b %RESULTADO%
