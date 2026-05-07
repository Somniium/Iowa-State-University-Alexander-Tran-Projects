module lab4step1 (A, F, C, G, W);
input F, C, G, W;
output A;
reg A; //procedural statements can only change the values of
//variables declared as memory elements such as a reg (register)
always @(F or C or G or W) //invoke procedural code when these change
begin //begin the procedural statements
case ({F,C,G,W}) //variables that affect the procedure

	4'b0000: A= 'b0;
	4'b0001: A= 'b0;
	4'b0010: A= 'b0;
	4'b0011: A= 'b1;
	4'b0100: A= 'b0;
	4'b0101: A= 'b0;
	4'b0110: A= 'b1;
	4'b0111: A= 'b1;
	4'b1000: A= 'b1;
	4'b1001: A= 'b1;
	4'b1010: A= 'b0;
	4'b1011: A= 'b0;
	4'b1100: A= 'b1;
	4'b1101: A= 'b0;
	4'b1110: A= 'b0;
	4'b1111: A= 'b0;
endcase
end //end the begin statement
endmodule