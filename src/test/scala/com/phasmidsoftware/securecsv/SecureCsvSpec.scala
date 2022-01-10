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

  it should "execute bad workflow 0" in {
    val args = Array[String]()
    SecureCsv.workflow(args) shouldBe false
  }

  it should "execute bad workflow 1" in {
    val args = Array("-a", "encrypt", "-f", "junk.csv")
    SecureCsv.workflow(args) shouldBe false
  }

  it should "execute good workflow 0" in {
    val args = Array("-f", "examples/TeamProject.csv")
    SecureCsv.workflow(args) shouldBe true
  }

  it should "execute good workflow 1" in {
    val args = Array("-a", "read", "-f", "examples/TeamProject.csv")
    SecureCsv.workflow(args) shouldBe true
  }

  it should "execute good workflow 2" in {
    val args = Array("-a", "decrypt", "-f", "examples/TeamProject.csv", "-r", "1", "-p", "k0JCcO$SY5OI50uj")
    SecureCsv.workflow(args) shouldBe true
  }

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
