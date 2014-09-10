#set logscale xy

set yzeroaxis
set ytics axis
set yrange [-500:500]

set multiplot
plot 'billClanLines.dat' using 1:2 lt rgb "green" w line
plot 'billClanLines.dat' using 1:3 lt rgb "red" w line
unset multiplot
