@echo OFF
if not exist ./Calculator.class javac ./Calculator.java
:A
set /p var=�����������ʽ:
java Calculator %var%


set/p input= �Ƿ����(y/n):
if "%input%"=="y" goto A
if "%input%"=="n" goto B

:B
pause
exit
