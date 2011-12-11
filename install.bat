@echo off
copy conf\sqltool.rc %UserProfile%
call startup.bat
call regendb.bat
