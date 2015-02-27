import play.PlayJava

name := """brahma"""

version := "0.3.0"

lazy val brahma = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  javaJpa.exclude("org.hibernate.javax.persistence", "hibernate-jpa-2.0-api"),
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
  "org.hibernate" % "hibernate-entitymanager" % "4.3.7.Final",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.paypal.sdk" % "rest-api-sdk" % "1.1.0",
  "redis.clients" % "jedis" % "2.6.0",
  "com.amazonaws" % "aws-java-sdk" % "1.9.22"
)