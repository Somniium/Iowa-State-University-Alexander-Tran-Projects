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
// CREATED		"Tue Apr 15 15:21:23 2025"

module lab10step2a(
	Enable,
	Clock,
	CLRN,
	Z,
	Q0,
	Q1,
	Q2,
	Q3
);


input wire	Enable;
input wire	Clock;
input wire	CLRN;
output wire	Z;
output wire	Q0;
output wire	Q1;
output wire	Q2;
output wire	Q3;

wire	SYNTHESIZED_WIRE_0;
wire	SYNTHESIZED_WIRE_14;
wire	SYNTHESIZED_WIRE_2;
reg	SYNTHESIZED_WIRE_15;
wire	SYNTHESIZED_WIRE_16;
reg	SYNTHESIZED_WIRE_17;
wire	SYNTHESIZED_WIRE_18;
wire	SYNTHESIZED_WIRE_6;
wire	SYNTHESIZED_WIRE_8;
reg	SYNTHESIZED_WIRE_19;
reg	SYNTHESIZED_WIRE_20;
wire	SYNTHESIZED_WIRE_21;

assign	Q0 = SYNTHESIZED_WIRE_19;
assign	Q1 = SYNTHESIZED_WIRE_20;
assign	Q2 = SYNTHESIZED_WIRE_15;
assign	Q3 = SYNTHESIZED_WIRE_17;
assign	SYNTHESIZED_WIRE_14 = 1;




always@(posedge Clock or negedge CLRN or negedge SYNTHESIZED_WIRE_14)
begin
if (!CLRN)
	begin
	SYNTHESIZED_WIRE_19 <= 0;
	end
else
if (!SYNTHESIZED_WIRE_14)
	begin
	SYNTHESIZED_WIRE_19 <= 1;
	end
else
	begin
	SYNTHESIZED_WIRE_19 <= SYNTHESIZED_WIRE_0;
	end
end


always@(posedge Clock or negedge CLRN or negedge SYNTHESIZED_WIRE_14)
begin
if (!CLRN)
	begin
	SYNTHESIZED_WIRE_20 <= 0;
	end
else
if (!SYNTHESIZED_WIRE_14)
	begin
	SYNTHESIZED_WIRE_20 <= 1;
	end
else
	begin
	SYNTHESIZED_WIRE_20 <= SYNTHESIZED_WIRE_2;
	end
end

assign	SYNTHESIZED_WIRE_6 = SYNTHESIZED_WIRE_15 ^ SYNTHESIZED_WIRE_16;

assign	SYNTHESIZED_WIRE_8 = SYNTHESIZED_WIRE_17 ^ SYNTHESIZED_WIRE_18;



always@(posedge Clock or negedge CLRN or negedge SYNTHESIZED_WIRE_14)
begin
if (!CLRN)
	begin
	SYNTHESIZED_WIRE_15 <= 0;
	end
else
if (!SYNTHESIZED_WIRE_14)
	begin
	SYNTHESIZED_WIRE_15 <= 1;
	end
else
	begin
	SYNTHESIZED_WIRE_15 <= SYNTHESIZED_WIRE_6;
	end
end


always@(posedge Clock or negedge CLRN or negedge SYNTHESIZED_WIRE_14)
begin
if (!CLRN)
	begin
	SYNTHESIZED_WIRE_17 <= 0;
	end
else
if (!SYNTHESIZED_WIRE_14)
	begin
	SYNTHESIZED_WIRE_17 <= 1;
	end
else
	begin
	SYNTHESIZED_WIRE_17 <= SYNTHESIZED_WIRE_8;
	end
end

assign	SYNTHESIZED_WIRE_21 = SYNTHESIZED_WIRE_19 & Enable;

assign	SYNTHESIZED_WIRE_16 = SYNTHESIZED_WIRE_20 & SYNTHESIZED_WIRE_21;

assign	SYNTHESIZED_WIRE_18 = SYNTHESIZED_WIRE_15 & SYNTHESIZED_WIRE_16;

assign	Z = SYNTHESIZED_WIRE_17 & SYNTHESIZED_WIRE_18;

assign	SYNTHESIZED_WIRE_0 = SYNTHESIZED_WIRE_19 ^ Enable;

assign	SYNTHESIZED_WIRE_2 = SYNTHESIZED_WIRE_20 ^ SYNTHESIZED_WIRE_21;


endmodule
