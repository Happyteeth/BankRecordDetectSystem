@echo off

REM setlocal enabledelayedexpansion


set OLD_APP_JAR=aml-web.jar
 REM APP_JAR=test_aml.jar
set APP_NAME=aml-web

set FILE_PID=%APP_NAME%.nohup

set JAVA_OPTION=
cd /d %~dp0 
set currentfolder=%cd%
cd %currentfolder%

:main

  
  echo current = %currentfolder%
  set command=%1
  
  call :parsepid
  
  if "%command%"=="" ( goto :printuse )
  
  if %command% == stop  ( call :stop )  else if %command% == start ( call :start ) else if %command% == restart ( call :start ) else if %command% == status ( call :status ) else  ( call :printuse )  
  
goto:eof


:start


REM 判断PID是否存在
call :stop


if not exist %OLD_APP_JAR% (
	echo %OLD_APP_JAR% 文件不存在
	exit -1
)

if  exist %APP_NAME%.nohup (
  if not exist logs (
    md %currentfolder%\logs
  )
  set THISDATE=%DATE:~0,4%%DATE:~5,2%%DATE:~8,2%
  move %APP_NAME%.nohup %currentfolder%\logs\%APP_NAME%%THISDATE%.nohup
)
echo "begin to start  start  java %JAVA_OPTION% -jar $OLD_APP_JAR"
start /b javaw %JAVA_OPTION% -jar %OLD_APP_JAR% 1>%APP_NAME%.nohup  2>&1 


  goto :eof




:status


goto :eof



:parsepid
	set str='FINDSTR /C:"with PID" %FILE_PID%'
	set count=1
	set numb=1
	set name="PID"

	:STR_VISTOR
	 for /f "tokens=1,*" %%a in (%str% ) do (

		set word=%%a
		

		set /a count += 1
		
		
		if  "%word%"=="PID"  (
		  set /a numb = %count% + 1
		)

		if "%count%"=="%numb%" (
		  set pid=%word%
		)
		
		echo "%count%  %numb%"
		
		
		set str="%%b"
		
		goto STR_VISTOR
	 )
	 echo pid="%pid%"
	 	 
 goto :eof
 
 
:stop
    call :parsepid
	if not "%pid%"=="" (
		TASKLIST /FI "PID eq %pid%" | FIND "%pid%"
		set result=%ERRORLEVEL%
		if "%ERRORLEVEL%" == "0" (
		  TASKKILL /FI "PID eq %pid%" /F
		 )
	)
  
  

goto:eof   


endlocal