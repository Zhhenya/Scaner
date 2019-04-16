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
	call main 


	invoke  ExitProcess,0  

	func proc 
		push ebp 
		mov ebp, esp 
		sub esp, [0 + 0] 

		mov eax, 5 
		mov ebx, 5 
		mul ebx 
		mov ebx, eax 
		mov eax, 5 
		mov ebx, 6 
		mul ebx 
		add eax, ebx 
		add eax, ebx 
		mov edx, 0 
		mov ebx, eax 
		mov eax, dword ptr [y] 
		mov ebx, 2 
		div ebx 
		add ebx, eax 
		sub ebx, 1 
		mov eax, ebx 
		jmp label_12 

		label_12: mov esp, ebp 
		pop ebp 
		ret 0 
	func endp 

	func2 proc 
		push ebp 
		mov ebp, esp 
		sub esp, [0 + 0] 

		mov dword ptr [tt], 0 

		mov dword ptr [yyy], 0 

		cmp dword ptr [r], 1 
		mov eax, dword ptr [r] 
	jg label_23 

	mov dword ptr [uuu], 0 

	mov eax, dword ptr [uuu] 
	add eax, 9 
	mov dword ptr [uuu], eax 

	label_23: nop 
		mov eax, dword ptr [r] 
		sub eax, 1 
		mov eax, eax 
		jmp label_27 

		label_27: mov esp, ebp 
		pop ebp 
		ret 0 
	func2 endp 

	main proc 
		push ebp 
		mov ebp, esp 
		sub esp, [0 + 0] 

		push 5 
		call func 

		mov dword ptr [k], eax 

		push 6 
		call func2 

		mov dword ptr [l], eax 

		label_37: mov esp, ebp 
		pop ebp 
		ret 0 
	main endp 
END  
