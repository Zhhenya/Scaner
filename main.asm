.386 
.MODEL flat, stdcall 

.DATA 
	y            DD 

_break       DD 13 

.CODE 
mov dword ptr [y], 0 

proc fact 
	push ebp 
	mov ebp, esp 
	sub esp, [0 + 0] 

	mov eax, 0 
	jmp label_9 

	mov eax, dword ptr [n] 
	mov ebx, 2 
	mul ebx 
	mov eax, eax 
	jmp label_9 

	label_9: mov esp, ebp 
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
	jmp label_19 

	label_19: mov esp, ebp 
	pop ebp 
	ret 0 
endp 
END  
