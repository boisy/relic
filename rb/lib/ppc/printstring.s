; Print a String
;
; Entry:
;   printstring_p0 - path
;   printstring_p1 - address of string to print
        .text
        .globl  printstring
        .align  2
printstring:  
; prologue: set up the stack frame
	mflr r0		; move link register to r0
	stw r0, 8(r1)   ; save r0 on stack
	stwu r1, -16(r1) ; adjust stack

; get path in r3
        addis r3,0,ha16(printstring_p0)
        lbz  r3,lo16(printstring_p0)(r3)

; get address in r4
        addis r4,0,ha16(printstring_p1)
        ori  r4,r4,lo16(printstring_p1)

; get length of string in r5
        addi r5,0,0     ; clear r5 (string length)
        mr   r7,r4      ; move pointer to string into r7
count:  lbz  r6,0(r7)
        cmpwi r6,0
        beq  lo 
        addi r7,r7,1
        addi r5,r5,1
        b    count 
        
lo:     
        li   r0,4       ; syscall number (sys_write)
        sc              ; call kernel 

        nop
        nop

; epilogue: tear down stack and return
	addi r1, r1, 16	; destroy the stack frame
	lwz r0, 8(r1)   ; get original link register value we saved earlier
	mtlr r0         ; place it back in link register
	blr		; return
