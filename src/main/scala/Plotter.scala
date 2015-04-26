package com.billding

case class PlotScript(data:List[String])

case class PlotProperties(
  yMax:Int = 500,
  yMin:Int = -500,
  xMax:Int = 0,
  xMin:Int = 0,
  numCols:Int = 3,
  filename: String = "blah.png"
)

case class GnuPlotter (
  yMax:Int = 500,
  yMin:Int = -500,
  xMax:Int = 0,
  xMin:Int = 0,
  numCols:Int = 2,
  filename: String = "programmatic.png",
  pp: PlotProperties = PlotProperties()
) {

  def createDynamicPlotFile(pp: PlotProperties) = {

  }

  def createPlotScript(project:String) = {
    val imageOutput = s"""
      set term png
      set output "images/$project.png"
    """

    val plotSettings = s"""
      set yzeroaxis
      set ytics axis
      set yrange [$yMin:$yMax]

      set multiplot
    """

    val startCol = 2
    val colors = List("green", "red", "pink", "blue")
    val colRange = Range(startCol, startCol + pp.numCols)
    println(s"pp.numCols: ${pp.numCols}")

    // After mapping elements from a range to their original values,
    // I have a vector of values, which I then convert to a List so that I can 
    // zip all specified columns with their respective colors
    //colRange.map(x=>x).toList zip colors foreach println

    val colsAndColors = colRange zip colors 
    println(s"colsAndColors: ${colsAndColors}")

    val totalPlotsReal = colsAndColors map{ entry => GnuPlotter.plotColumn(project, entry._1, entry._2) } 

    //println(s"totalPlotsReal: ${totalPlotsReal.reduce(_ + "\n" + _)}")


    val endPlotSettings = """unset multiplot"""

    val plotScriptData = List(imageOutput + plotSettings + totalPlotsReal.reduce(_ + "\n" + _) + endPlotSettings)
    PlotScript(plotScriptData)
  }
}

object GnuPlotter extends Client{
  val program = Seq("gnuplot")

  val plotFileDirectory = Seq("plotfiles/*")

  val persistentArguments = plotFileDirectory

  def plotColumn(project:String, column:Int, color:String):String = {
    "plot 'data/" + project + ".dat' using 1:" + column + " lt rgb \"" + color + "\" w line \n"
  }

  def executePlotScripts() = {
    execute()
  }

}

