name := "Stream Mashup"

organization := "org.haffla"

version := "0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

scalaVersion := "2.11.7"

scalacOptions ++= Seq(
  "-feature"
)

libraryDependencies ++= Seq(
  cache,
  ws,
  specs2 % Test,
  "org.squeryl" %% "squeryl" % "0.9.5-7",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalikejdbc" %% "scalikejdbc" % "2.2.9",
  "org.scalikejdbc" %% "scalikejdbc-config" % "2.2.9",
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.4.3",
  "org.scalatestplus" %% "play" % "1.4.0-M3" % "test",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars.bower" % "progressbar.js" % "0.9.0",
  "org.webjars" % "bootstrap" % "3.1.1-2",
  "org.webjars" % "react" % "0.13.3",
  "org.webjars" % "font-awesome" % "4.4.0",
  "org.webjars" % "lodash" % "3.10.1",
  play.sbt.Play.autoImport.cache,
  "com.github.haffla" %% "soundcloud-scala" % "0.1-SNAPSHOT",
  evolutions
)

val alternateTestOptions = "-Dconfig.file=conf/" + Option(System.getProperty("test.config")).getOrElse("application") + ".conf"
javaOptions in Test += alternateTestOptions

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

routesGenerator := InjectedRoutesGenerator

unmanagedResourceDirectories in Compile += baseDirectory.value / "app" / "views"
