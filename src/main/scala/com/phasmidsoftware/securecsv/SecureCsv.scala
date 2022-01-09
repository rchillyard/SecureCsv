/*
 * Copyright (c) 2022-2022. Phasmid Software
 */

package com.phasmidsoftware.securecsv

import com.phasmidsoftware.RawRow
import com.phasmidsoftware.parse.{TableParser, TableParserException}
import com.phasmidsoftware.table.{HeadedTable, Table}

import java.io.File
import scala.util.{Failure, Success, Try}

/**
 * SecureCsv.
 *
 * @param table the table.
 * @tparam T the underlying row type.
 */
case class SecureCsv[T](table: HeadedTable[T]) {
}

object SecureCsv {
  def parsePlaintextTable[T](file: File)(implicit tp: TableParser[Table[T]]): Try[SecureCsv[T]] = Table.parseFile(file) flatMap {
    case x: HeadedTable[_] => Success(SecureCsv(x))
    case _ => Failure(TableParserException("parsedTable is not headed"))
  }

  def parsePlaintextRowTable(file: File)(implicit tp: TableParser[Table[RawRow]]): Try[SecureCsv[RawRow]] = {
    import com.phasmidsoftware.parse.RawParsers.WithHeaderRow._
    parsePlaintextTable(file)
  }

}
