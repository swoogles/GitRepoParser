set yzeroaxis
set ytics axis
set yrange [-1000:1000]

set multiplot
plot 'billClanLines.dat' using 1:2
plot 'billClanLines.dat' using 1:3
unset multiplot