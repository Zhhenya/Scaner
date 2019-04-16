.386 
.MODEL flat, stdcall 

_break       DD 13 

.CODE 
proc fact 
	push ebp 
	mov ebp, esp 
	sub esp, [0 + 0] 

jg label_6 

mov eax, 0 
jmp label_10 

label_6: nop 
	mov eax, dword ptr [n] 
	mov ebx, 2 
	mul ebx 
	mov eax, eax 
	jmp label_10 

	label_10: mov esp, ebp 
	pop ebp 
	ret 0 
endp 

proc main 
	push ebp 
	mov ebp, esp 
	sub esp, [0 + 0] 

	push 6 
	call fact 

	add eax, dword ptr [y] 
	mov dword ptr [y], eax 

	mov eax, 0 
	jmp label_20 

	label_20: mov esp, ebp 
	pop ebp 
	ret 0 
endp 
END  
