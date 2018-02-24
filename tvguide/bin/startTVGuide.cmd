set ROOT=C:\Documents and Settings\All Users\Documents\Website\tvguide
pushd "%ROOT%\bin"
rem /D"%ROOT%" /MIN /LOW
start "TVGuide Update" /D"%ROOT%\bin" /MIN /LOW doTVGuide.cmd

