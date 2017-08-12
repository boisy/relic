; Compare a String
;
; Entry:
;   r3 - string1
;   r4 - string2
; Exit:
;   r3 = -1 (r3 < r4), 0 (r3 = r4), 1 (r3 > r4)
        .text
        .align  2
        .globl  comparestring
comparestring:  
; prologue: set up the stack frame
	mflr r0		; move link register to r0
	stw r0, 8(r1)   ; save r0 on stack
	stwu r1, -16(r1) ; adjust stack

next:
        lbz  r5,0(r3)
        addi r3,r3,1
		
        lbz  r6,0(r4)
        addi r4,r4,1

        cmp  0,0,r5,r6 
        bgt  higher
        blt  lower
        cmpi 0,0,r5,0
        bne  next
       
equal:
        li   r3,0
        b    epilogue
higher:
        li   r3,0x1
        b    epilogue
lower:
        li   r3,0xFF

; epilogue: tear down stack and return
epilogue:
	addi r1, r1, 16	; destroy the stack frame
	lwz r0, 8(r1)   ; get original link register value we saved earlier
	mtlr r0         ; place it back in link register
	blr		; return



; Compare a String without respect to case
;
; Entry:
;   r3 - string1
;   r4 - string2
; Exit:
;   r3 = -1 (r3 < r4), 0 (r3 = r4), 1 (r3 > r4)
        .text
        .align  2
        .globl  comparestringnocase
comparestringnocase:  
; prologue: set up the stack frame
	mflr r0		; move link register to r0
	stw r0, 8(r1)   ; save r0 on stack
	stwu r1, -16(r1) ; adjust stack

next1:
        lbz  r5,0(r3)
        addi r3,r3,1
        cmpi 0,0,r5,'A
	    blt  next2
        cmpi 0,0,r5,'Z
	    bgt  next2
		ori  r5,r5,0x20
		
next2:
        lbz  r6,0(r4)
        addi r4,r4,1
        cmpi 0,0,r6,'A
	    blt  compare
        cmpi 0,0,r6,'Z
	    bgt  compare
		ori  r6,r6,0x20

compare:
        cmp  0,0,r5,r6 
        bgt  higher1
        blt  lower1
        cmpi 0,0,r5,0
        bne  next1
       
equal1:
        li   r3,0
        b    epilogue
higher1:
        li   r3,0x1
        b    epilogue
lower1:
        li   r3,0xFF
        b    epilogue
