package com.billding

case class GnuPlotter (
  yMax:Int = 500,
  yMin:Int = -500,
  xMax:Int = 0,
  xMin:Int = 0,
  numCols:Int = 2
)

object GnuPlotter {
  def createPlotScript(plotter:GnuPlotter, project:String) = {
    val plotSettings = """
    set yzeroaxis
    set ytics axis
    set yrange [""" + plotter.yMin + ":" + plotter.yMax + """]

    set multiplot
    """

    val line1 = "plot '../data/" + project + ".dat' using 1:2 lt rgb \"green\" w line \n"
    val line2 = "plot '../data/" + project + ".dat' using 1:3 lt rgb \"red\" w line \n"

    val endPlotSettings = """unset multiplot"""

    plotSettings + line1 + line2 + endPlotSettings
    
  }
}

