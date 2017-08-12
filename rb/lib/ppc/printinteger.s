; Print an integer
;
; Entry:
;   printinteger_p0 - path
;   printinteger_p1 - integer to print
        .data
        .comm   temp,8

        .text
        .align  2
        .globl  printinteger
printinteger:  
; prologue: set up the stack frame
	mflr r0		; move link register to r0
	stw r0, 8(r1)   ; save r0 on stack
	stwu r1, -16(r1) ; adjust stack

; get integer to print into r3
	addis r3,0,ha16(printinteger_p1)
        lhz   r3,lo16(printinteger_p1)(r3)

; get pointer to temp buffer in r4
	addis r4,0,ha16(temp)
        ori   r4,r4,lo16(temp)

; call decS16toStr
        bl    decS16toStr

; get path in r3
        addis r3,0,ha16(printinteger_p0)
        lbz  r3,lo16(printinteger_p0)(r3)

; get pointer to temp buffer in r4
	addis r4,0,ha16(temp)
        ori   r4,r4,lo16(temp)

; get length of string in r5
        addi r5,0,0     ; clear r5 (string length)
        mr   r7,r4      ; move pointer to string into r7
count:  lbz  r6,0(r7)
        cmpwi r6,0
        beq  lo
        addi r7,r7,1
        addi r5,r5,1
        b    count

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
