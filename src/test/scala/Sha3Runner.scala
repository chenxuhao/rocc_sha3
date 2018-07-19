// See LICENSE.txt for license details.
package sha3

import scala.collection.mutable.ArrayBuffer
import chisel3.iotesters._

object Sha3Runner {
  def apply(section: String, sha3Map: Map[String, TesterOptionsManager => Boolean], args: Array[String]): Unit = {
    var successful = 0
    val errors = new ArrayBuffer[String]

    val optionsManager = new TesterOptionsManager()
    optionsManager.doNotExitOnHelp()

    optionsManager.parse(args)

    val programArgs = optionsManager.commonOptions.programArgs
    if(programArgs.isEmpty) {
      println("Available modules")
      for(x <- sha3Map.keys) {
        println(x)
      }
      println("all")
      System.exit(0)
    }

    val problemsToRun = if(programArgs.exists(x => x.toLowerCase() == "all")) {
      sha3Map.keys
    }
    else {
      programArgs
    }
    //println(s"cxh debug programArgs=$programArgs")
    //println(s"cxh debug problemsToRun=$problemsToRun")

    for(testName <- problemsToRun) {
      sha3Map.get(testName) match {
        case Some(test) =>
          println(s"Starting module $testName")
          try {
            optionsManager.setTopName(testName)
            optionsManager.setTargetDirName(s"test_run_dir/$section/$testName")
            if(test(optionsManager)) {
              successful += 1
            }
            else {
              errors += s"Sha3 $testName: test error occurred"
            }
          }
          catch {
            case exception: Exception =>
              exception.printStackTrace()
              errors += s"Sha3 $testName: exception ${exception.getMessage}"
            case t : Throwable =>
              errors += s"Sha3 $testName: throwable ${t.getMessage}"
          }
        case _ =>
          errors += s"Bad module name: $testName"
      }

    }
    if(successful > 0) {
      println(s"Sha3 passing: $successful")
    }
    if(errors.nonEmpty) {
      println("=" * 80)
      println(s"Errors: ${errors.length}: in the following modules")
      println(errors.mkString("\n"))
      println("=" * 80)
      System.exit(1)
    }
  }
}
