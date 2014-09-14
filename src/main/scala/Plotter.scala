package com.billding

object GnuPlotter {
  def createPlotScript(project:String) = {
    val plotSettings = """
    set yzeroaxis
    set ytics axis
    set yrange [-500:500]

    set multiplot
    """

    val line1 = "plot '../data/" + project + ".dat' using 1:2 lt rgb \"green\" w line \n"
    val line2 = "plot '../data/" + project + ".dat' using 1:3 lt rgb \"red\" w line \n"

    val endPlotSettings = """unset multiplot"""

    plotSettings + line1 + line2 + endPlotSettings
    
  }
}

