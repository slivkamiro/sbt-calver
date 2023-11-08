
inThisBuild(List(
  organization := "io.github.slivkamiro",
  homepage := Some(url("https://github.com/slivkamiro/sbt-calver")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "slivkamiro",
      "Miroslav Slivka",
      "mi.slivka@gmail.com",
      url("https://github.com/slivkamiro")
    )
  )
))

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-calver",
    scriptedLaunchOpts   += s"-Dplugin.version=${version.value}",
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.2.8" // set minimum sbt version
      }
    }
  )
