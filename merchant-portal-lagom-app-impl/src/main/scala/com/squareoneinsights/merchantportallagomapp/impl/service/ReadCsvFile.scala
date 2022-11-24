package com.squareoneinsights.merchantportallagomapp.impl.service

import java.io.File
import scala.util.{Failure, Success, Try}

object ReadCsvFile extends App {


  private def validateEntry(csvRecord: Map[String, String]) = {
    val result = csvRecord("name").nonEmpty && csvRecord("last").nonEmpty
    if (!result) {
      println(s"name: ${csvRecord("name")}, amountLimit: ${csvRecord("last")}")
    }
    result
  }

  val file = new File("/media/tushar/7440C38440C34C14/BACKUP/workSpaceIntelligeIFRM/ifrm-api/ifrm-lists-and-limits-impl/src/main/resources/testCsv.csv")
  println("---------->" + file)

  val reader: CSVReader = Try(CSVReader.open(file))
  match {
    case Success(value) => {
      println(s"csv read successfully"+value)
      value
    }
    case Failure(exception) => {
      println(s"Error while reading csv \n ${exception.getMessage}")
      throw exception
    }
  }

  println("===>"+reader.allWithHeaders().filter(validateEntry))
}
