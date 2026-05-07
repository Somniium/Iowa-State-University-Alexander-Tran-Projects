// Copyright (C) 2024  Intel Corporation. All rights reserved.
// Your use of Intel Corporation's design tools, logic functions 
// and other software and tools, and any partner logic 
// functions, and any output files from any of the foregoing 
// (including device programming or simulation files), and any 
// associated documentation or information are expressly subject 
// to the terms and conditions of the Intel Program License 
// Subscription Agreement, the Intel Quartus Prime License Agreement,
// the Intel FPGA IP License Agreement, or other applicable license
// agreement, including, without limitation, that your use is for
// the sole purpose of programming logic devices manufactured by
// Intel and sold by Intel or its authorized distributors.  Please
// refer to the applicable agreement for further details, at
// https://fpgasoftware.intel.com/eula.

// PROGRAM		"Quartus Prime"
// VERSION		"Version 23.1std.1 Build 993 05/14/2024 SC Standard Edition"
// CREATED		"Tue Mar 25 14:36:00 2025"

module lab7addersub(
	X3,
	Y3,
	X2,
	Y2,
	X1,
	Y1,
	X,
	Y,
	Control,
	Overflow,
	Cout,
	S3,
	S2,
	S1,
	S0
);


input wire	X3;
input wire	Y3;
input wire	X2;
input wire	Y2;
input wire	X1;
input wire	Y1;
input wire	X;
input wire	Y;
input wire	Control;
output wire	Overflow;
output wire	Cout;
output wire	S3;
output wire	S2;
output wire	S1;
output wire	S0;

wire	SYNTHESIZED_WIRE_0;
wire	SYNTHESIZED_WIRE_1;
wire	SYNTHESIZED_WIRE_2;
wire	SYNTHESIZED_WIRE_3;




assign	SYNTHESIZED_WIRE_0 = Control ^ Y3;

assign	SYNTHESIZED_WIRE_1 = Control ^ Y2;

assign	SYNTHESIZED_WIRE_2 = Control ^ Y1;

assign	SYNTHESIZED_WIRE_3 = Control ^ Y;


lab74bit	b2v_inst4(
	.X3(X3),
	.Y3(SYNTHESIZED_WIRE_0),
	.X2(X2),
	.Y2(SYNTHESIZED_WIRE_1),
	.X1(X1),
	.Y1(SYNTHESIZED_WIRE_2),
	.Cin(Control),
	.X(X),
	.Y(SYNTHESIZED_WIRE_3),
	.Ov(Overflow),
	.Cout(Cout),
	.S3(S3),
	.S2(S2),
	.S1(S1),
	.S0(S0));


endmodule
