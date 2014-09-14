package com.billding

case class GnuPlotter (
  yMax:Int = 500,
  yMin:Int = -500,
  xMax:Int = 0,
  xMin:Int = 0,
  numCols:Int = 2
)

object GnuPlotter {
  def plotColumn(project:String, column:Int, color:String):String = {
    "plot '../data/" + project + ".dat' using 1:" + column + " lt rgb \"" + color + "\" w line \n"

  }

  def createPlotScript(plotter:GnuPlotter, project:String) = {
    val plotSettings = """
    set yzeroaxis
    set ytics axis
    set yrange [""" + plotter.yMin + ":" + plotter.yMax + """]

    set multiplot
    """

    val line1 = plotColumn(project, 2, "green")
    val line2 = plotColumn(project, 3, "red")

    val endPlotSettings = """unset multiplot"""

    plotSettings + line1 + line2 + endPlotSettings
    
  }
}

