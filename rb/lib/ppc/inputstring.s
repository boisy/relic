; Input a String
;
; Entry:
;   inputstring_p0 - path
;   inputstring_p1 - address of string to input
;   inputstring_p2 - maximum length of string to accept
        .text
        .globl  inputstring
        .align  2
inputstring:  
; prologue: set up the stack frame
	mflr r0		; move link register to r0
	stw r0, 8(r1)   ; save r0 on stack
	stwu r1, -16(r1) ; adjust stack

; get path in r3
        addis r3,0,ha16(inputstring_p0)
        lbz  r3,lo16(inputstring_p0)(r3)

; get address in r4
        addis r4,0,ha16(inputstring_p1)
        ori  r4,r4,lo16(inputstring_p1)

; get length of string in r5
        addis r5,0,ha16(inputstring_p2)
        lhz  r5,lo16(inputstring_p2)(r5)

        li   r0,3       ; syscall number (sys_read)
        sc              ; call kernel 

        nop
        nop

        addis r4,0,ha16(inputstring_p1)
        ori  r4,r4,lo16(inputstring_p1)
		add  r4,r4,r3
		subi r4,r4,1
		li   r5,0
		stb  r5,0(r4)

; epilogue: tear down stack and return
	addi r1, r1, 16	; destroy the stack frame
	lwz r0, 8(r1)   ; get original link register value we saved earlier
	mtlr r0         ; place it back in link register
	blr		; return
