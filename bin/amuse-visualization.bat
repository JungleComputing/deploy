@echo off

if "%OS%"=="Windows_NT" @setlocal

rem %~dp0 is expanded pathname of the current script under NT

if "%DEPLOY_HOME%X"=="X" set DEPLOY_HOME=%~dp0..

set DEPLOY_ARGS=

:setupArgs
if ""%1""=="""" goto doneStart
set DEPLOY_ARGS=%DEPLOY_ARGS% %1
shift
goto setupArgs

:doneStart

if "%PROCESSOR_ARCHITECTURE%"=="AMD64" goto 64BIT_ARCHITECTURE
java -Xmx500m -splash:images\ibis-splash.png -classpath "%CLASSPATH%;%DEPLOY_HOME%;%DEPLOY_HOME%\lib\*" -Dibis.deploy.home="%DEPLOY_HOME%" -Djava.library.path="%DEPLOY_HOME%"\lib\natives amuseVisualization.AmuseVisualization %DEPLOY_ARGS%
goto END_ARCHITECTURE
:64BIT_ARCHITECTURE
java -Xmx500m -splash:images\ibis-splash.png -classpath "%CLASSPATH%;%DEPLOY_HOME%;%DEPLOY_HOME%\lib\*" -Dibis.deploy.home="%DEPLOY_HOME%" -Djava.library.path="%DEPLOY_HOME%"\lib\natives amuseVisualization.AmuseVisualization %DEPLOY_ARGS%
:END_ARCHITECTURE

if "%OS%"=="Windows_NT" @endlocal
