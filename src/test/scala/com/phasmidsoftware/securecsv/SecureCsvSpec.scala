/*
 * Copyright (c) 2022-2022. Phasmid Software
 */

package com.phasmidsoftware.securecsv

import com.phasmidsoftware.RawRow
import com.phasmidsoftware.parse.TableParser
import com.phasmidsoftware.securecsv.examples.{TeamProject, TeamProjectTableParser}
import com.phasmidsoftware.table.Table
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import java.io.File
import scala.util.{Success, Try}

class SecureCsvSpec extends AnyFlatSpec with should.Matchers {

  behavior of "SecureCsv"

  it should "parsePlaintextTable" in {
    implicit val teamProjectParser: TableParser[Table[TeamProject]] = TeamProjectTableParser
    val filename = "examples/TeamProject.csv"
    val psy: Try[SecureCsv[TeamProject]] = SecureCsv.parsePlaintextTable(new File(filename))
    psy should matchPattern { case Success(SecureCsv(_)) => }
    for (ps <- psy) {
      ps.table.size shouldBe 5
      ps.table foreach println
    }
  }

  it should "parsePlaintextRowTable" in {
    val filename = "examples/TeamProject.csv"
    val psy: Try[SecureCsv[RawRow]] = SecureCsv.parsePlaintextRowTable(new File(filename), 2)
    psy should matchPattern { case Success(SecureCsv(_)) => }
    for (ps <- psy) {
      ps.table.size shouldBe 5
      ps.table foreach println
    }
  }

}
