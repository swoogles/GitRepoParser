package com.billding

import com.billding.git.OutputDirectories

case class PlotScript(data:List[String])

case class PlotProperties(
  yMax:Int = 10,
  yMin:Int = 0,
  numCols:Int = 1
)

class Plotter(val outputDirs: OutputDirectories) extends Client{
  val program = Seq("gnuplot")
  val persistentArguments = Seq(outputDirs.plotFiles + "/*")

  def plotColumn(project:String, column:Int, color:String):String = {
    s"""plot '${outputDirs.dataFiles}/$project.dat' using 1:$column lt rgb "$color" w line \n"""
  }

  def executePlotScripts() = {
    execute()
  }

  def createPlotScript(project:String, pp: PlotProperties) = {
    val outputImg = outputDirs.images / (project + ".png")
    // TODO use new Path for images
    val imageOutput = s"""
      set term png
      set output "$outputImg"
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

    val totalPlotsReal = colsAndColors map{ entry => plotColumn(project, entry._1, entry._2) }

    val endPlotSettings = """unset multiplot"""

    val plotScriptData = List(imageOutput + plotSettings + totalPlotsReal.reduce(_ + "\n" + _) + endPlotSettings)
    PlotScript(plotScriptData)
  }

}

