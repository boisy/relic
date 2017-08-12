; Print a boolean
;
; Entry:
;   printboolean_p0 - path
;   printboolean_p1 - boolean to print

        .text
true:   .ascii  "True"
trulen  =       .-true

false:  .ascii  "False"
fallen  =       .-false

        .align  2
        .globl  printboolean
printboolean:  
; prologue: set up the stack frame
	mflr r0		; move link register to r0
	stw r0, 8(r1)   ; save r0 on stack
	stwu r1, -16(r1) ; adjust stack

; determine value in r3
        addi r3,0,ha16(printboolean_p1)
        lbz  r3,lo16(printboolean_p1)(r3)
        cmpi 0,0,r3,0
        bne  gotrue

; get pointer to true in r4
gofalse: addis r4,0,ha16(false)
        ori   r4,r4,lo16(false)
        li    r5,fallen
        b     getpath

gotrue:	addis r4,0,ha16(true)
        ori   r4,r4,lo16(true)
        li    r5,trulen

; get path in r3
getpath:
        addi r3,0,ha16(printboolean_p0)
        lbz  r3,lo16(printboolean_p0)(r3)

; call system call to write
lo:     li    r0,4
        sc   

        nop
        nop

; epilogue: tear down stack and return
	addi r1, r1, 16	; destroy the stack frame
	lwz r0, 8(r1)   ; get original link register value we saved earlier
	mtlr r0         ; place it back in link register
	blr		; return
