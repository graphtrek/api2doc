@echo off

set API2DOC_APPDIR=%~dp0
set API2DOC_LIBDIR=%API2DOC_APPDIR%lib

set PARAMS=%*

SETLOCAL ENABLEDELAYEDEXPANSION
set CLASSPATH=
for %%f in (%API2DOC_LIBDIR%\*.*) do set CLASSPATH=!CLASSPATH!%%f;

java -classpath "%CLASSPATH%" co.grtk.api2doc.Api2DocApplication
ENDLOCAL
