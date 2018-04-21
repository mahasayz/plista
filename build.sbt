name := "plista"

version := "0.1"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  val akkaVersion = "2.4.12"
  Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.1",
    "com.typesafe.akka" %% "akka-http" % "10.1.1",
    "com.typesafe.akka" %% "akka-stream" % "2.5.11",
    "org.json4s" %% "json4s-native" % "3.3.0",
    "org.jsoup" % "jsoup" % "1.8.3",
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
    "commons-validator" % "commons-validator" % "1.5+",
    "com.outr" %% "lucene4s" % "1.6.0",
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  )
}

// Assembly settings
mainClass in assembly := Some("com.solution.core.Main")

assemblyJarName in assembly := "plista-test.jar"