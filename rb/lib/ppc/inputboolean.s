; Input a Boolean
;
; Entry:
;   inputboolean_p0 - path
;   inputboolean_p1 - address of string to input
        .data
        .comm   inputbooleanbuffer,7,0
		
        .text
        .globl  inputboolean
BadInput:   .ascii    "** Input error - BOOLEAN requires TRUE/1 or FALSE/0 - reenter **"
			.byte     0x0D,0x0A
BadInputLen =         .-BadInput
TrueTRUE:   .ascii    "true"
            .byte     0x00
True1:      .ascii    "1"
            .byte     0x00
FalseFALSE: .ascii    "false"
            .byte     0x00
False0:     .ascii    "0"
            .byte     0x00

		.align	2
inputboolean:  
; prologue: set up the stack frame
		mflr r0		; move link register to r0
		stw r0, 8(r1)   ; save r0 on stack
		stwu r1, -16(r1) ; adjust stack

; get path in r3
again:
        addis r3,0,ha16(inputboolean_p0)
        lbz  r3,lo16(inputboolean_p0)(r3)

; get address in r4
        addis r4,0,ha16(inputbooleanbuffer)
        ori  r4,r4,lo16(inputbooleanbuffer)

; get length of string in r5
        li   r5,6

        li   r0,3       ; syscall number (sys_read)
        sc              ; call kernel 

        nop
        nop

; replace CR with nul byte
        addis r4,0,ha16(inputbooleanbuffer)
        ori  r4,r4,lo16(inputbooleanbuffer)
        add  r4,r4,r3
		subi r4,r4,1
		li   r5,0
		stb  r5,0(r4)
		
; determine input (0 or FALSE, 1 or TRUE)
        addis   r3,0,ha16(inputbooleanbuffer)
        ori     r3,r3,lo16(inputbooleanbuffer)
        addis   r4,0,ha16(TrueTRUE)
		ori     r4,r4,lo16(TrueTRUE)
 		bl      comparestringnocase
		cmpi    0,0,r3,0
		beq     isTrue
		
        addis   r3,0,ha16(inputbooleanbuffer)
        ori     r3,r3,lo16(inputbooleanbuffer)
        addis   r4,0,ha16(True1)
		ori     r4,r4,lo16(True1)
 		bl      comparestringnocase
		cmpi    0,0,r3,0
		beq     isTrue

        addis   r3,0,ha16(inputbooleanbuffer)
        ori     r3,r3,lo16(inputbooleanbuffer)
        addis   r4,0,ha16(FalseFALSE)
		ori     r4,r4,lo16(FalseFALSE)
 		bl      comparestringnocase
		cmpi    0,0,r3,0
		beq     isFalse
		
        addis   r3,0,ha16(inputbooleanbuffer)
        ori     r3,r3,lo16(inputbooleanbuffer)
        addis   r4,0,ha16(False0)
		ori     r4,r4,lo16(False0)
 		bl      comparestringnocase
		cmpi    0,0,r3,0
		beq     isFalse

; redo
        li      r3,2
		
        addis   r4,0,ha16(BadInput)
        ori     r4,r4,lo16(BadInput)
		
		li      r5,BadInputLen
		
		li      r0,4
		sc
		
		nop
		nop
		
		b       again

isTrue:
        li      r3,1
		b       saveit

isFalse:
        li      r3,0
saveit:
        addis   r4,0,ha16(inputboolean_p1)
        ori     r4,r4,lo16(inputboolean_p1)
		stb     r3,0(r4)
		
; epilogue: tear down stack and return
epilogue:
		addi r1, r1, 16	; destroy the stack frame
		lwz r0, 8(r1)   ; get original link register value we saved earlier
		mtlr r0         ; place it back in link register
		blr		; return

		
