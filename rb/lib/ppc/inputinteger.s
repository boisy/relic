; Input an integer
;
; Entry:
;   inputinteger_p0 - path
;   inputinteger_p1 - address of integer to input

        .text

badmsg:      .ascii  "** Input error - reenter **"
              .byte    0x0D,0x0A
badmsglen   = .-badmsg

        .globl  inputinteger
        .align  2
inputinteger:  
; prologue: set up the stack frame
	mflr r0		; move link register to r0
	stw r0, 8(r1)   ; save r0 on stack
	stwu r1, -16(r1) ; adjust stack

; get path in r3
tryagain:
        addis r3,0,ha16(inputinteger_p0)
        lbz  r3,lo16(inputinteger_p0)(r3)

; get address in r4
        addis r4,0,ha16(inputinteger_p1)
        ori  r4,r4,lo16(inputinteger_p1)

; get length of string in r5
        li   r5,6      ; -XXXXX maximum

        li   r0,3       ; syscall number (sys_read)
        sc              ; call kernel 

        nop
        nop

; call to convert
        addis r3,0,ha16(inputinteger_p1)
        ori  r3,r3,lo16(inputinteger_p1)
        bl   StrToDecS16
        cmpi 0,0,r3,0
        beq  saveit
        bl   RedoInput
        b    tryagain

saveit:
        addis r3,0,ha16(inputinteger_p1)
        ori  r3,r3,lo16(inputinteger_p1)
        sth  r4,0(r3)
        
; epilogue: tear down stack and return
epilogue:
	addi r1, r1, 16	; destroy the stack frame
	lwz r0, 8(r1)   ; get original link register value we saved earlier
	mtlr r0         ; place it back in link register
	blr		; return

RedoInput:
; prologue: set up the stack frame
	mflr r0		; move link register to r0
	stw r0, 8(r1)   ; save r0 on stack
	stwu r1, -16(r1) ; adjust stack

	li   r3,2       ; stderr
        addis r4,0,ha16(badmsg) 
        ori  r4,r4,lo16(badmsg) 
        li   r5,badmsglen
        li   r0,4
        sc
        nop 
        nop 
        b    epilogue


