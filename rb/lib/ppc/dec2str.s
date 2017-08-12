; Convert an unsigned 8-bit decimal number to a string
;
; Entry:
;   r3 - 8 bit integer to convert
;   r4 - address of 4 byte buffer
; Exit:
;   r4 - address of nul byte
        .text
        .align  2
        .globl  decU8toStr

decU8toStr:
; prologue: set up the stack frame
	mflr r0		; move link register to r0
	stw r0, 8(r1)   ; save r0 on stack
	stwu r1, -16(r1) ; adjust stack

        li    r5,0
        b     byte 

; Convert a signed 16-bit decimal number to a string
;
; Entry:
;   r3 - 16 bit integer to convert
;   r4 - address of 8 byte buffer
; Exit:
;   r4 - address of nul byte

        .globl  decS16toStr
        .align  2
decS16toStr:  

; determine if integer is negative; if not, go unsigned right away
        cmpwi r3,0x7FFF
        ble  decU16toStr

; add negative sign
        addi r5,0,'-
        stb  r5,0(r4)
        addi r4,r4,1

; make twos complement of number
        addi r0,0,0
        orc  r3,r0,r3
        addi r3,r3,1
        andi. r3,r3,0x7FFF

; Convert an unsigned 16-bit decimal number to a string
;
; Entry:
;   r3 - 16 bit integer to convert
;   r4 - address of 6 byte buffer
; Exit:
;   r4 - address of nul byte

        .globl  decU16toStr
        .align  2
decU16toStr:  
; prologue: set up the stack frame
	mflr r0		; move link register to r0
	stw r0, 8(r1)   ; save r0 on stack
	stwu r1, -16(r1) ; adjust stack

        li    r5,0
        li    r7,10000
        bl    conv
        li    r7,1000
        bl    conv
byte:   li    r7,100
        bl    conv
        li    r7,10
        bl    conv
        li    r7,1
        bl    conv
        cmpi  0,0,r5,0
        bne   addnil
        li    r7,0x30
        stb   r7,0(r4)
        addi  r4,r4,1
 
addnil: li    r7,0
        stb   r7,0(r4)

; epilogue: tear down stack and return
	addi r1, r1, 16	; destroy the stack frame
	lwz r0, 8(r1)   ; get original link register value we saved earlier
	mtlr r0         ; place it back in link register
	blr		; return

conv:   li    r8,0x30
l1:     sub   r3,r3,r7
        cmpi  0,0,r3,0
        blt   k1
        addi  r8,r8,1
        b     l1
k1:     add   r3,r3,r7 
        cmpi  0,0,r8,0x30
        bne   l2
        cmpi  0,0,r5,0
        bne   l2
        blr      
l2:     stb   r8,0(r4)
        addi  r4,r4,1
        li    r5,1
        blr
