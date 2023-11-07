import scala.sys.process.stringToProcess
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset

def check(a: String, e: String) = assert(a == e, s"Version mismatch: Expected '$e', Incoming '$a'")

def tstamp = Def.setting(me.slivkamiro.calver.CalverPlugin timestamp calverDate.value)

def datetag = Def.setting(calverDate.value.toInstant().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(versionFormat.value)))

def headSha = Def.task("git rev-parse --short=8 HEAD".!!.trim)


TaskKey[Unit]("checkUntaggedRepository")                     := check(version.value, s"${datetag.value}.1-${headSha.value}")
TaskKey[Unit]("checkUncommittedChangesOnUntaggedRepository") := check(version.value, s"${datetag.value}.1-${headSha.value}-${tstamp.value}")
TaskKey[Unit]("checkAtTheOldTag") := check(version.value, s"${datetag.value}.1")
TaskKey[Unit]("checkUncomittedAtTheOldTag") := check(version.value, s"${datetag.value}.1-${headSha.value}-${tstamp.value}")
TaskKey[Unit]("checkAheadOfTheOldTag") := check(version.value, s"${datetag.value}.1-${headSha.value}")
TaskKey[Unit]("checkAtTheTag") := check(version.value, s"${datetag.value}.1")
TaskKey[Unit]("checkUncomitted") := check(version.value, s"${datetag.value}.2-${headSha.value}-${tstamp.value}")
TaskKey[Unit]("checkAhead") := check(version.value, s"${datetag.value}.2-${headSha.value}")


TaskKey[Unit]("gitInit") := {
  git("init")(streams.value.log)
  git("config user.email calver@example.com")(streams.value.log)
  git("config user.name calver")(streams.value.log)
}

TaskKey[Unit]("gitStatus") := git("status")(streams.value.log)

TaskKey[Unit]("gitDescribe") := git("describe --long --tags --abbrev=8 --always --dirty")(streams.value.log)

TaskKey[Unit]("gitCommit") := {
  git("add build.sbt")(streams.value.log)
  git("commit -am \"1\"")(streams.value.log)
}

TaskKey[Unit]("gitTagOld") := git(s"""tag -a v23.01.1 -m "v0"""")(streams.value.log)

TaskKey[Unit]("gitTag") := git(s"""tag -a v${datetag.value}.1 -m "v1"""")(streams.value.log)

TaskKey[Unit]("changes") := {
  import java.nio.file._, StandardOpenOption._
  import scala.collection.JavaConverters._
  Files.write(baseDirectory.value.toPath.resolve("f.txt"), Seq("1").asJava, CREATE, APPEND)
  git("add f.txt")(streams.value.log)
}

TaskKey[Unit]("changes2") := {
  import java.nio.file._, StandardOpenOption._
  import scala.collection.JavaConverters._
  Files.write(baseDirectory.value.toPath.resolve("f.txt"), Seq("2").asJava, CREATE, APPEND)
  git("add f.txt")(streams.value.log)
}

TaskKey[Unit]("changes3") := {
  import java.nio.file._, StandardOpenOption._
  import scala.collection.JavaConverters._
  Files.write(baseDirectory.value.toPath.resolve("f.txt"), Seq("3").asJava, CREATE, APPEND)
  git("add f.txt")(streams.value.log)
}

def git(command: String)(log: Logger): Unit = {
  log info s"git $command"
  log info s"git $command".!!
}
