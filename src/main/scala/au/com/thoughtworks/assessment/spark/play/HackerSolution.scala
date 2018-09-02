package au.com.thoughtworks.assessment.spark.play


import java.io._
import java.math._
import java.security._
import java.text._
import java.util._
import java.util.concurrent._
import java.util.function._
import java.util.regex._
import java.util.stream._

import scala.collection.immutable._
import scala.collection.mutable._
import scala.collection.concurrent._
import scala.collection.parallel.immutable._
import scala.collection.parallel.mutable._
import scala.concurrent._
import scala.io._
import scala.math._
import scala.sys._
import scala.util.matching._
import scala.reflect._
import scala.tools.nsc.ConsoleWriter

object HackerSolution {

  // Complete the compareTriplets function below.
  def compareTriplets(a: Array[Int], b: Array[Int]): Array[Int] = {
    var acount, bcount  = 0


    for ( i <- a.indices) {
      if (a(i) > b(i)) acount += 1
      else if (a(i) < b(i)) bcount += 1
    }
    println (acount + " " + bcount)
    Array(acount, bcount)
  }

  def main(args: Array[String]) {
//    val printWriter = new PrintWriter(sys.env("OUTPUT_PATH"))
    val printWriter = new ConsoleWriter ()

    val a = StdIn.readLine.replaceAll("\\s+$", "").split(" ").map(_.trim.toInt)

    val b = StdIn.readLine.replaceAll("\\s+$", "").split(" ").map(_.trim.toInt)
    val result = compareTriplets(a, b)

    printWriter.write(result.mkString(" "))

    printWriter.close()
  }
}
