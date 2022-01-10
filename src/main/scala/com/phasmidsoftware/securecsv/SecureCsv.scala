/*
 * Copyright (c) 2022-2022. Phasmid Software
 */

package com.phasmidsoftware.securecsv

import com.phasmidsoftware.RawRow
import com.phasmidsoftware.args.Args
import com.phasmidsoftware.parse.{EncryptedHeadedStringTableParser, RawParsers, TableParser, TableParserException}
import com.phasmidsoftware.table.Table.parseResource
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
    val say = Args.parse(args, Some("[-a [action]] -f filename [-r row] [-p password] [-o filename] [-n rows]"))
    tryToOption(say) match {
      case Some(as) if as.nonEmpty => // NOTE: we shouldn't have to check this if parse working OK
        val a = as.getArgValue("a").getOrElse("read")
        val rsy = (a, as.getArgValue("f"), as.getArgValue("r"), as.getArgValue("p")) match {
          case ("decrypt", Some(filename), Some(row), Some(password)) =>
            parseEncryptedRowTable(new File(filename), as.getArgValueAsY("n").getOrElse(1), row, password)
          case (_, Some(filename), _, _) =>
            parsePlaintextRowTable(new File(filename), as.getArgValueAsY("n").getOrElse(1)) // XXX ignore row
          case _ =>
            Failure(new Exception("workflow: logic error"))
        }
        val rso = tryToOption(rsy)
        (rso, as.getArgValue("a").getOrElse("read"), as.getArgValue("o")) match {
          case (Some(rs), "encrypt", Some(o)) =>
            true // output encrypted file
          case (Some(rs), _, _) => //rs.table.toCSV
            true
          case _ =>
            false
        }
      case _ =>
        false
    }
  }

  def parsePlaintextTable[T](file: File)(implicit tp: TableParser[Table[T]]): Try[SecureCsv[T]] = Table.parseFile(file) flatMap {
    case x: HeadedTable[_] => Success(SecureCsv(x))
    case _ => Failure(TableParserException("parsedTable is not headed"))
  }

  def parseEncryptedRowTable(file: File, headerRows: Int, row: String, password: String): Try[SecureCsv[RawRow]] = {
    implicit object RawRowTableParser extends RawParsers(None, false, headerRows)
    import RawRowTableParser.RawTableParser

    //    val keyMap = Map("1" -> "k0JCcO$SY5OI50uj", "2" -> "QwSeQVJNuAg6D6H9", "3" -> "dTLsxr132eucgu10", "4" -> "mexd0Ta81di$fCGp", "5" -> "cb0jlsf4DXtZz_kf")

    def encryptionPredicate(w: String): Boolean = w == row

    import RawParsers.WithHeaderRow.rawRowCellParser

    implicit val parser: TableParser[Table[RawRow]] = EncryptedHeadedStringTableParser[RawRow](encryptionPredicate, _ => password, headerRowsToRead = headerRows)
    val pty: Try[Table[RawRow]] = parseResource("TeamProjectEncrypted.csv", classOf[SecureCsv.type])

    parsePlaintextTable(file)
  }

  def parsePlaintextRowTable(file: File, headerRows: Int): Try[SecureCsv[RawRow]] = {
    implicit object RawRowTableParser extends RawParsers(None, false, headerRows)
    import RawRowTableParser.RawTableParser
    parsePlaintextTable(file)
  }

  private def tryToOption[X](xy: Try[X]): Option[X] = FP.tryToOption(x => logger.warn(x.getLocalizedMessage))(xy)

  private val logger: Logger = org.slf4j.LoggerFactory.getLogger(classOf[SecureCsv.type])
}
