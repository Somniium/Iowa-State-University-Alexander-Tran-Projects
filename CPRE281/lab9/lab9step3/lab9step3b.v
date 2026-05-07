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
// CREATED		"Tue Apr  8 14:54:09 2025"

module lab9step3b(
	D,
	Clk,
	Qn,
	Q
);


input wire	D;
input wire	Clk;
output wire	Qn;
output wire	Q;

wire	SYNTHESIZED_WIRE_10;
wire	SYNTHESIZED_WIRE_11;
wire	SYNTHESIZED_WIRE_12;
wire	SYNTHESIZED_WIRE_3;
wire	SYNTHESIZED_WIRE_6;
wire	SYNTHESIZED_WIRE_9;

assign	Qn = SYNTHESIZED_WIRE_6;
assign	Q = SYNTHESIZED_WIRE_9;



assign	SYNTHESIZED_WIRE_12 = ~(D & SYNTHESIZED_WIRE_10);

assign	SYNTHESIZED_WIRE_10 = ~(SYNTHESIZED_WIRE_11 & Clk & SYNTHESIZED_WIRE_12);

assign	SYNTHESIZED_WIRE_11 = ~(Clk & SYNTHESIZED_WIRE_3);

assign	SYNTHESIZED_WIRE_3 = ~(SYNTHESIZED_WIRE_11 & SYNTHESIZED_WIRE_12);

assign	SYNTHESIZED_WIRE_9 = ~(SYNTHESIZED_WIRE_6 & SYNTHESIZED_WIRE_11);

assign	SYNTHESIZED_WIRE_6 = ~(SYNTHESIZED_WIRE_10 & SYNTHESIZED_WIRE_9);


endmodule
