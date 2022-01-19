/*
 * Copyright (c) 2022-2022. Phasmid Software
 */

package com.phasmidsoftware.securecsv

import com.phasmidsoftware.crypto.{EncryptionUTF8AES128CTR, HexEncryption}
import com.phasmidsoftware.parse.TableParser
import com.phasmidsoftware.securecsv.examples.{TeamProject, TeamProjectTableParser}
import com.phasmidsoftware.table.{RawRow, Table}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import java.io.File
import scala.util.{Success, Try}

class SecureCsvSpec extends AnyFlatSpec with should.Matchers {

  behavior of "SecureCsv"

  type Algorithm = tsec.cipher.symmetric.jca.AES128CTR

  implicit val encryption: HexEncryption[Algorithm] = EncryptionUTF8AES128CTR
  implicit val tableEncryption: TableEncryption[Algorithm] = new TableEncryption[Algorithm]

  it should "execute bad workflow 0" in {
    val args = Array[String]()
    SecureCsv.workflow(args) shouldBe false
  }

  it should "execute bad workflow 1" in {
    val args = Array("-a", "encrypt", "-f", "junk.csv")
    SecureCsv.workflow(args) shouldBe false
  }

  it should "execute good workflow 0" in {
    val args = Array("-f", "examples/TeamProject.csv", "-n", "2")
    SecureCsv.workflow(args) shouldBe true
  }

  it should "execute good workflow 1" in {
    val args = Array("-a", "read", "-f", "examples/TeamProject.csv", "-n", "2")
    SecureCsv.workflow(args) shouldBe true
  }

  it should "execute good workflow 2a" in {
    val args = Array("-a", "decrypt", "-f", "examples/TeamProjectEncrypted.csv", "-r", "1", "-k", "Team Number", "-p", "o2AzFGjVMiPCkIVm", "-n", "2")
    SecureCsv.workflow(args) shouldBe true
  }

  it should "execute good workflow 2b" in {
    val args = Array("-a", "decrypt", "-f", "examples/TeamProjectEncrypted.csv", "-o", "output/TeamProject.csv", "-r", "5", "-p", "JOvJCG3sSrZHAdv3", "-n", "2")
    SecureCsv.workflow(args) shouldBe true
  }

  // TODO Issue #1
  it should "execute good workflow 3a" in {
    val args = Array("-a", "encrypt", "-f", "examples/TeamProject.csv", "-k", "0", "-o", "output/TeamProjectEncrypted.csv", "-n", "2")
    SecureCsv.workflow(args) shouldBe true
  }

  it should "execute good workflow 3b" in {
    val args = Array("-a", "encrypt", "-f", "examples/TeamProjectWithNewlines.csv", "-o", "output/TeamProjectEncrypted.csv", "-n", "2", "-m")
    SecureCsv.workflow(args) shouldBe true
  }

  it should "parseCsvFile" in {
    implicit val teamProjectParser: TableParser[Table[TeamProject]] = TeamProjectTableParser
    val filename = "examples/TeamProject.csv"
    val psy: Try[SecureCsv[TeamProject]] = tableEncryption.parseCsvFile(new File(filename))
    psy should matchPattern { case Success(SecureCsv(_)) => }
    for (ps <- psy) {
      ps.table.size shouldBe 5
      ps.table foreach println
    }
  }

  it should "parsePlaintextRowTable" in {
    val filename = "examples/TeamProject.csv"
    val psy: Try[SecureCsv[RawRow]] = tableEncryption.parsePlaintextRowTable(new File(filename), 2, multiline = false)
    psy should matchPattern { case Success(SecureCsv(_)) => }
    for (ps <- psy) {
      ps.table.size shouldBe 5
      ps.table foreach println
    }
  }

}
