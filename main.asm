.386 
.MODEL flat, stdcall 
option casemap :none 

include e:\masm32\include\windows.inc 
include e:\masm32\include\masm32.inc 
include e:\masm32\include\msvcrt.inc 
include e:\masm32\include\kernel32.inc 

includelib e:\masm32\lib\msvcrt.lib 
includelib e:\masm32\lib\kernel32.lib 
includelib e:\masm32\lib\masm32.lib 

.DATA 
	_break       DD 13 

.CODE 
start: 
END  
