module mux4to1 (w0,w1,w2,w3,s,f);
input w0,w1,w2,w3;
input [1:0] s;
output f;

assign f = s[1]?(s[0]?w3:w2):(s[0]?w1:w0);

endmodule