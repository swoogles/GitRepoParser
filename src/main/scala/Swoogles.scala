package com.billding

import ammonite.ops.Path


class Utility() {
  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
      val p = new java.io.PrintWriter(f)
        try { op(p) } finally { p.close() }
  }
}

class DataWriter() {
  def write(data:List[String], outputFile:String, utility:Utility):String = {
    import java.io._
    utility.printToFile(new File(outputFile))(p => {
        data.foreach(p.println)
    })
    "incomplete"
  }
  def write(data:List[String], outputFile: Path, utility:Utility):String = {
    utility.printToFile(outputFile.toIO)(p => {
      data.foreach(p.println)
    })
    "incomplete"
  }
}
