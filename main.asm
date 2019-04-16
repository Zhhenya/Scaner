.386 
.MODEL flat, stdcall 

_break       DD 13 

.CODE 
proc func 
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
endp 

proc func2 
	push ebp 
	mov ebp, esp 
	sub esp, [0 + 0] 

jg label_20 

mov eax, dword ptr [r] 
jmp label_24 

label_20: nop 
	mov eax, dword ptr [r] 
	sub eax, 1 
	mov eax, eax 
	jmp label_24 

	label_24: mov esp, ebp 
	pop ebp 
	ret 0 
endp 

proc main 
	push ebp 
	mov ebp, esp 
	sub esp, [0 + 0] 

	push 5 
	call func 

	mov dword ptr [k], eax 

	push 6 
	call func2 

	mov dword ptr [l], eax 

	label_34: mov esp, ebp 
	pop ebp 
	ret 0 
endp 
END  
