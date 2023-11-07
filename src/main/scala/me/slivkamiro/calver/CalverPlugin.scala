package me.slivkamiro.calver

import sbt._
import sbt.Keys._
import java.util.Date
import java.time.format.DateTimeFormatter

import scala.sys.process.Process
import scala.util.Try
import java.time.ZoneOffset

object CalverPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  private lazy val buildBase = ThisBuild / baseDirectory

  object autoImport {
    val versionFormat = settingKey[String](
      "Version format, e.g. YY.MM will produce version like 23.11.n where n is a patch number"
    )
    val calverDate =
      settingKey[Date]("Current date, for Calver internal purposes")
  }

  import autoImport._

  override def buildSettings: Seq[Setting[_]] = Seq(
    versionFormat := "YY.0M",
    calverDate := new Date,
    version := getVersion.value(
      buildBase.value,
      calverDate.value,
      versionFormat.value
    )
  )

  private lazy val getVersion = Def.setting {
    (wd: File, date: Date, format: String) =>
      val formattedDate = date
        .toInstant()
        .atOffset(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern(versionFormat.value))
      val describe = getGitDescribeOutput(wd, date)

      describe.fold(formattedDate + ".1")(_.calver(formattedDate)).trim
  }

  val FullTag =
    """^v(.+)\.([0-9]+)-([0-9]+)-g([0-9a-f]{8})-?([0-9]{8}-[0-9]{4})?$""".r

  private def getGitDescribeOutput(
      wd: File,
      d: Date
  ): Option[GitDescribeOutput] = {
    val process = Process(
      s"git describe --long --tags --abbrev=8 --always --dirty=-${timestamp(d)}",
      wd
    )
    Try(process.!!.trim).toOption
      .map {
        case FullTag(version, patch, "0", _, null) =>
          GitDescribeOutput.Head(version, patch.toInt)
        case FullTag(version, patch, _, sha, null) =>
          GitDescribeOutput.CommitWithDistanceFromTag(version, patch.toInt, sha)
        case FullTag(version, patch, _, sha, dirtyTag) =>
          GitDescribeOutput.UncommittedChanges(
            version,
            patch.toInt,
            sha,
            dirtyTag
          )
        case sha =>
          GitDescribeOutput.ShaOnly(sha)
      }
  }

  def timestamp(d: Date): String = f"$d%tY$d%tm$d%td-$d%tH$d%tM"

  sealed trait GitDescribeOutput {
    def version: String
    def patch: Int

    def calver(dateVersion: String): String = {
      val p = if (dateVersion == version) patch else 1
      s"$dateVersion.$p"
    }
  }

  object GitDescribeOutput {
    final case class Head(version: String, patch: Int) extends GitDescribeOutput
    final case class CommitWithDistanceFromTag(
        version: String,
        patch: Int,
        sha: String
    ) extends GitDescribeOutput {
      override def calver(dateVersion: String): String = {
        val p = if (dateVersion == version) patch + 1 else 1
        s"$dateVersion.$p-$sha"
      }
    }
    final case class UncommittedChanges(
        version: String,
        patch: Int,
        sha: String,
        dirtyTag: String
    ) extends GitDescribeOutput {
      override def calver(dateVersion: String): String = {
        val p = if (dateVersion == version) patch + 1 else 1
        s"$dateVersion.$p-$sha-$dirtyTag"
      }
    }
    final case class ShaOnly(sha: String) extends GitDescribeOutput {
      override val version: String = sha
      override val patch: Int = 1

      override def calver(dateVersion: String): String =
        s"$dateVersion.$patch-$sha"
    }
  }
}
