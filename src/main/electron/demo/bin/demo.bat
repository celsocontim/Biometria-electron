@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  demo startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and DEMO_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\tutorial.jar;%APP_HOME%\lib\jetty-webapp-9.3.20.v20170531.jar;%APP_HOME%\lib\websocket-server-9.3.20.v20170531.jar;%APP_HOME%\lib\jetty-servlet-9.3.20.v20170531.jar;%APP_HOME%\lib\jetty-security-9.3.20.v20170531.jar;%APP_HOME%\lib\jetty-server-9.3.20.v20170531.jar;%APP_HOME%\lib\websocket-servlet-9.3.20.v20170531.jar;%APP_HOME%\lib\javax.servlet-api-3.1.0.jar;%APP_HOME%\lib\vaadin-server-8.1.6.jar;%APP_HOME%\lib\vaadin-push-8.1.6.jar;%APP_HOME%\lib\vaadin-client-compiled-8.1.6.jar;%APP_HOME%\lib\vaadin-themes-8.1.6.jar;%APP_HOME%\lib\jetty-continuation-9.3.20.v20170531.jar;%APP_HOME%\lib\vaadin-sass-compiler-0.9.13.jar;%APP_HOME%\lib\vaadin-shared-8.1.6.jar;%APP_HOME%\lib\jsoup-1.8.3.jar;%APP_HOME%\lib\gentyref-1.2.0.vaadin1.jar;%APP_HOME%\lib\atmosphere-runtime-2.4.11.vaadin2.jar;%APP_HOME%\lib\jetty-http-9.3.20.v20170531.jar;%APP_HOME%\lib\websocket-client-9.3.20.v20170531.jar;%APP_HOME%\lib\websocket-common-9.3.20.v20170531.jar;%APP_HOME%\lib\jetty-io-9.3.20.v20170531.jar;%APP_HOME%\lib\jetty-xml-9.3.20.v20170531.jar;%APP_HOME%\lib\sac-1.3.jar;%APP_HOME%\lib\flute-1.3.0.gg2.jar;%APP_HOME%\lib\vaadin-slf4j-jdk14-1.6.1.jar;%APP_HOME%\lib\jetty-util-9.3.20.v20170531.jar;%APP_HOME%\lib\websocket-api-9.3.20.v20170531.jar


@rem Execute demo
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %DEMO_OPTS%  -classpath "%CLASSPATH%" demo.Launcher %*

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable DEMO_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%DEMO_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
