@echo off

rem The format of %TIME% is HH:MM:SS,CS for example 23:59:59,99
set STARTTIME=%TIME%
set appName=backend
set fileName=%appName%.tar
set dir=%~dp0
set path=%dir%backend\build\distributions\%fileName%
set host=pi@192.168.0.16
set remoteLoc=/home/pi/Desktop
set scp=C:\Windows\System32\OpenSSH\scp.exe
set ssh=C:\Windows\System32\OpenSSH\ssh.exe
set remoteScript=/home/pi/Desktop/run_java.sh

goto :Main

:Main
    if [%1%]==[] (
        call :build_execute
        goto :eof
    )
    if %~1==-sb (
        call :exec_program_on_pi
    ) else (
        echo unknown command
    )
goto :eof

:build_execute
    call :build_project
    call :copy_build_to_pi
    call :print_exec_time
    call :exec_program_on_pi
goto :eof


:build_project
    setlocal
    set execRemote=gradlew clean installDist
    echo "exec build -> %execRemote%"
    call %execRemote%
    echo "build completed"
    endlocal
goto :eof

:copy_build_to_pi
    setlocal
    set execRemote=%scp% %path% %host%:%remoteLoc%
    echo "exec copy -> %execRemote%"
    call %execRemote%
    endlocal
goto :eof

:exec_program_on_pi
    setlocal
    set execRemote=%ssh% %host% %remoteScript% %appName%
    echo "exec remote script -> %execRemote%"
    call %execRemote%
    endlocal
goto :eof

:print_exec_time
    setlocal
    set ENDTIME=%TIME%
    echo STARTTIME: %STARTTIME%
    echo ENDTIME: %ENDTIME%
    set /A STARTTIME=(1%STARTTIME:~0,2%-100)*360000 + (1%STARTTIME:~3,2%-100)*6000 + (1%STARTTIME:~6,2%-100)*100 + (1%STARTTIME:~9,2%-100)
    set /A ENDTIME=(1%ENDTIME:~0,2%-100)*360000 + (1%ENDTIME:~3,2%-100)*6000 + (1%ENDTIME:~6,2%-100)*100 + (1%ENDTIME:~9,2%-100)
    set /A DURATION=(%ENDTIME%-%STARTTIME%)/100
    echo DURATION: %DURATION% in seconds
    endlocal
goto :eof