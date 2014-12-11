name := """brahma"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  javaJpa,
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
  "org.hibernate" % "hibernate-entitymanager" % "4.3.7.Final"
)
