package au.com.thoughtworks.assessment.spark.play

import java.io._
import java.math._
import java.security._
import java.text._
import java.util
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

object ScalaTry {
  def main(args: Array[String]) {
    val arr: Array[Int] =  Array(1, 2,4)
    // val a = if (arr.contains(2)) "YES" else "NO"
    val l: Int = 2
    val r: Int = 5
    var out: Array[Int] = Array()


    val sentences: Array[String] =  Array("jim likes mary", "kate likes tom", "tom does not like jim")

    val queries: Array[String] =  Array("jim tom", "likes")




//    sentenceWords.zipWithIndex.map(item => { queryWords.map(queryWord =>    {    if (item._1 == "jim") item._2 else None} ) })

    for (query <- queries) {
        sentences.map (sentence => {

          // for each sentence
//          val queryWords = query.split(" ")
          var queryWordsCount  = 0

          query.split(" ").map(queryWord => {
            val sentenceWords = sentence.split(" ")

            if (sentenceWords.contains(queryWord)) println (sentenceWords.indexOf(queryWord))
          })
//          if (queryWordsCount == query.split(" ").length) println (" found all")

//          val index = sentenceWords.zipWithIndex.map(item => {
//            if (item._1 == queryWord) item._2 else None
//          }).filter(_ != None).mkString(" ").map(print(_))
        })


      println(" ")
      }
  }
}
