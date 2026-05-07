module lab3step1(C,G,W,f);
input C,G,W;
output f;
wire g,h,i,j,k;

not(i, C);
not(j, G);
not(k, W);

or(g,C,j,W);
or(h,i,G,k);
and(f,g,h);

endmodule