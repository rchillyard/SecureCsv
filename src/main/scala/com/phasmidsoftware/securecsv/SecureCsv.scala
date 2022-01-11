/*
 * Copyright (c) 2022-2022. Phasmid Software
 */

package com.phasmidsoftware.securecsv

import com.phasmidsoftware.RawRow
import com.phasmidsoftware.args.Args
import com.phasmidsoftware.parse.TableParser.includeAll
import com.phasmidsoftware.parse._
import com.phasmidsoftware.render.{CsvGenerators, CsvRenderer, CsvRenderers}
import com.phasmidsoftware.securecsv.SecureCsv.workflow
import com.phasmidsoftware.table._
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
    val say = Args.parse(args, Some("[-a [action]] -f filename [-r row] [-p password] [-o filename] [-n rows] [-m] [-d delimiter]"), optionalProgramName = Some("SecureCsv"))
    tryToOption(say) match {
      case Some(as) =>
        val n = as.getArgValueAs[Int]("n").getOrElse(1)
        val m = as.isDefined("m")
        val rsy = (as.getArgValue("a").getOrElse("read"), as.getArgValueAs[File]("f"), as.getArgValue("r"), as.getArgValue("p")) match {
          case ("decrypt", Some(file), Some(row), Some(password)) =>
            parseEncryptedRowTable(file, n, row, password)
          case (_, Some(file), _, _) =>
            parsePlaintextRowTable(file, n, m) // XXX ignore row
          case _ =>
            Failure(new Exception("workflow: logic error"))
        }
        val rso = tryToOption(rsy)
        (rso, as.getArgValue("a").getOrElse("read"), as.getArgValueAs[File]("o")) match {
          case (Some(rs), "encrypt", Some(file)) =>
            writeEncrypted(rs, file)
          case (Some(rs), "decrypt", Some(file)) =>
            writePlaintext(rs, file)
          case (Some(_), "encrypt", _) =>
            false // corresponds to analyzing encrypted file.
          case (Some(rs), "decrypt", _) =>
            toCSV(rs)
          case (Some(rs), _, _) =>
            println(Analysis(rs.table).showColumnMap)
            true
          case _ =>
            false
        }
      case _ =>
        false
    }
  }

  /**
   * TODO rename this to parseCsvFile
   *
   * @param file the file to be parsed.
   * @param tp the (implicit) TableParser.
   * @tparam T the underlying row type.
   * @return a Try of SecureCsv[T]
   */
  def parsePlaintextTable[T](file: File)(implicit tp: TableParser[Table[T]]): Try[SecureCsv[T]] = Table.parseFile(file) flatMap {
    case x: HeadedTable[_] => Success(SecureCsv(x))
    case _ => Failure(TableParserException("parsedTable is not headed"))
  }

  def parseEncryptedRowTable(file: File, headerRows: Int, row: String, password: String): Try[SecureCsv[RawRow]] = {
    def encryptionPredicate(w: String): Boolean = w == row

    implicit val cellParser: CellParser[RawRow] = RawParsers.WithHeaderRow.rawRowCellParser
    implicit val parser: TableParser[Table[RawRow]] = EncryptedHeadedStringTableParser[RawRow](encryptionPredicate, _ => password, headerRowsToRead = headerRows)
    parsePlaintextTable(file)
  }

  def parsePlaintextRowTable(file: File, headerRows: Int, multiline: Boolean): Try[SecureCsv[RawRow]] = {
    implicit val parser: RawTableParser = com.phasmidsoftware.parse.RawTableParser(includeAll, None, forgiving = false, multiline = multiline, headerRows)
    parsePlaintextTable(file)
  }

  def writePlaintext(secureCsv: SecureCsv[RawRow], file: File): Boolean = {
    def createCsvRendererForRawRow: CsvRenderer[RawRow] = {
      import CsvRenderers._
      new CsvRenderers {}.sequenceRenderer
    }

    implicit val csvGenerator: CsvGenerator[RawRow] = secureCsv.table.maybeHeader match {
      case Some(h) => Row.csvGenerator(h) // NOTE: should always have header
      case None => new CsvGenerators {}.sequenceGenerator
    }
    implicit val csvRenderer: CsvRenderer[RawRow] = createCsvRendererForRawRow
    implicit val hasKey: HasKey[RawRow] = (t: RawRow) => t.head
    secureCsv.table.writeCSVFile(file)
    true
  }

  def toCSV(secureCsv: SecureCsv[RawRow]): Boolean = {
    import CsvRenderers._
    implicit val csvRenderer: CsvRenderer[RawRow] = new CsvRenderers {}.sequenceRenderer
    implicit val z: CsvGenerator[RawRow] = Row.csvGenerator(secureCsv.table.header)
    val w = secureCsv.table.toCSV
    println(w)
    w.nonEmpty
  }

  /**
   * CONSIDER merging with writePlaintext
   *
   * @param secureCsv the secureCsv to be written out.
   * @param file      the file to which the CSV file should be written.
   * @return true if successful.
   */
  def writeEncrypted(secureCsv: SecureCsv[RawRow], file: File): Boolean = {
    def createCsvRendererForRawRow: CsvRenderer[RawRow] = {
      import CsvRenderers._
      new CsvRenderers {}.sequenceRenderer
    }

    implicit val csvGenerator: CsvGenerator[RawRow] = secureCsv.table.maybeHeader match {
      case Some(h) => Row.csvGenerator(h) // NOTE: should always have header
      case None => new CsvGenerators {}.sequenceGenerator
    }
    implicit val csvRenderer: CsvRenderer[RawRow] = createCsvRendererForRawRow
    implicit val hasKey: HasKey[RawRow] = (t: RawRow) => t.head
    secureCsv.table.writeCSVFileEncrypted(file)
    true
  }

  private def tryToOption[X](xy: Try[X]): Option[X] = FP.tryToOption(x => logger.warn(x.getLocalizedMessage))(xy)

  private val logger: Logger = org.slf4j.LoggerFactory.getLogger(classOf[SecureCsv.type])
}

object Workflow extends App {
  if (workflow(args)) println("OK") else println("an error occurred: see logs")
}