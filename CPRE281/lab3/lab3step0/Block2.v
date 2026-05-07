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
// CREATED		"Tue Feb 11 15:42:09 2025"

module Block2(
	Cabbage,
	Goat,
	Wolf,
	pin_name1
);


input wire	Cabbage;
input wire	Goat;
input wire	Wolf;
output wire	pin_name1;

wire	SYNTHESIZED_WIRE_0;
wire	SYNTHESIZED_WIRE_1;
wire	SYNTHESIZED_WIRE_2;
wire	SYNTHESIZED_WIRE_3;
wire	SYNTHESIZED_WIRE_4;




assign	SYNTHESIZED_WIRE_3 = SYNTHESIZED_WIRE_0 | Wolf | Cabbage;

assign	SYNTHESIZED_WIRE_4 = Goat | SYNTHESIZED_WIRE_1 | SYNTHESIZED_WIRE_2;

assign	SYNTHESIZED_WIRE_2 =  ~Cabbage;

assign	SYNTHESIZED_WIRE_0 =  ~Goat;

assign	SYNTHESIZED_WIRE_1 =  ~Wolf;

assign	pin_name1 = SYNTHESIZED_WIRE_3 & SYNTHESIZED_WIRE_4;


endmodule
