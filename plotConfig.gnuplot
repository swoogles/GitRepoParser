#set logscale xy

set yzeroaxis
set ytics axis
set yrange [-500:500]

set multiplot
plot 'Repositories_Physics.dat' using 1:2 lt rgb "green" w line
plot 'Repositories_Physics.dat' using 1:3 lt rgb "red" w line
unset multiplot
