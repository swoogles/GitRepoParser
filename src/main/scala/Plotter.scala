package com.billding

case class PlotScript(data:List[String])

case class PlotProperties(
  yMax:Int = 10,
  yMin:Int = 0,
  numCols:Int = 1
)

object Plotter extends Client{
  val program = Seq("gnuplot")

  val plotFileDirectory = Seq("plotfiles/*")

  val persistentArguments = plotFileDirectory

  def plotColumn(project:String, column:Int, color:String):String = {
    "plot 'data/" + project + ".dat' using 1:" + column + " lt rgb \"" + color + "\" w line \n"
  }

  def executePlotScripts() = {
    execute()
  }

  def createPlotScript(project:String, pp: PlotProperties) = {
    val imageOutput = s"""
      set term png
      set output "images/$project.png"
    """

    val plotSettings = s"""
      set yzeroaxis
      set ytics axis
      set yrange [${pp.yMin}:${pp.yMax}]

      set multiplot
    """

    val startCol = 2
    val colors = List("green", "red", "pink", "blue")
    val colRange = Range(startCol, startCol + pp.numCols)

    val colsAndColors = colRange zip colors 

    val totalPlotsReal = colsAndColors map{ entry => Plotter.plotColumn(project, entry._1, entry._2) } 

    val endPlotSettings = """unset multiplot"""

    val plotScriptData = List(imageOutput + plotSettings + totalPlotsReal.reduce(_ + "\n" + _) + endPlotSettings)
    PlotScript(plotScriptData)
  }

}

