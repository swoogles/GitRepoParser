package com.billding

case class GnuPlotter (
  yMax:Int = 500,
  yMin:Int = -500,
  xMax:Int = 0,
  xMin:Int = 0,
  numCols:Int = 2
)

object GnuPlotter {
  val filename = "programmatic.png"

  val imageOutput = s"""
    set term png
    set output "$filename"
  """

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

    val startCol = 2
    val colors = List("green", "red")
    val colRange = Range(startCol, startCol + plotter.numCols)

    // After mapping elements from a range to their original values,
    // I have a vector of values, which I then convert to a List so that I can 
    // zip all specified columns with their respective colors
    //colRange.map(x=>x).toList zip colors foreach println

    val colsAndColors = colRange zip colors 

    val totalPlotsReal = colsAndColors map{ entry => plotColumn(project, entry._1, entry._2) } 

    val endPlotSettings = """unset multiplot"""

    imageOutput + plotSettings + totalPlotsReal.reduce(_ + "\n" + _) + endPlotSettings
  }
}

