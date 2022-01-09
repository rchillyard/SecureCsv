/*
 * Copyright (c) 2022-2022. Phasmid Software
 */

package com.phasmidsoftware.securecsv

import com.phasmidsoftware.RawRow
import com.phasmidsoftware.args.Args
import com.phasmidsoftware.parse.{RawParsers, TableParser, TableParserException}
import com.phasmidsoftware.table.{HeadedTable, Table}
import com.phasmidsoftware.util.FP
import org.slf4j.Logger

import java.io.File
import scala.util.{Failure, Success, Try}

/**
 * SecureCsv.
 *
 * @param table the table.
 * @tparam T the underlying row type.
 */
case class SecureCsv[T](table: HeadedTable[T])

object SecureCsv {
  def workflow(args: Array[String]): Boolean = {
    val say = Args.parse(args, Some("[-a action] -f filename [-o filename] [-n rows]"))
    tryToOption(say) match {
      case Some(args) if args.nonEmpty => // NOTE: we shouldn't have to check this if parse working OK
        val n: Int = args.getArgValueAsY("n").getOrElse(1)
        val oo: Option[String] = args.getArgValue("o")
        val rsy = (args.getArgValue("a").getOrElse("encrypt"), args.getArgValue("f")) match {
          case ("encrypt", Some(filename)) => parsePlaintextRowTable(new File(filename), n)
          case _ => Failure(new Exception("workflow: logic error"))
        }
        val rso = tryToOption(rsy)
        (rso, oo) match {
          case (Some(rs), Some(o)) => true // output encrypted file
          case (Some(rs), _) => //rs.table.toCSV
            true
          case _ => false
        }
      case _ => false
    }
  }

  def parsePlaintextTable[T](file: File)(implicit tp: TableParser[Table[T]]): Try[SecureCsv[T]] = Table.parseFile(file) flatMap {
    case x: HeadedTable[_] => Success(SecureCsv(x))
    case _ => Failure(TableParserException("parsedTable is not headed"))
  }

  def parsePlaintextRowTable(file: File, headerRows: Int): Try[SecureCsv[RawRow]] = {
    implicit object RawRowTableParser extends RawParsers(None, false, headerRows)
    import RawRowTableParser.RawTableParser
    parsePlaintextTable(file)
  }

  private def tryToOption[X](xy: Try[X]): Option[X] = FP.tryToOption(x => logger.warn(x.getLocalizedMessage))(xy)

  private val logger: Logger = org.slf4j.LoggerFactory.getLogger(classOf[SecureCsv.type])
}
