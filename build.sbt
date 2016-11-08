name := "play-websocket-scala"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"


// scalaz-bintray resolver needed for specs2 library
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

libraryDependencies += ws

libraryDependencies += "org.webjars" % "flot" % "0.8.3"
libraryDependencies += "org.webjars" % "bootstrap" % "3.3.6"

lazy val akkaVersion = "2.4.11"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23"
libraryDependencies += "com.thenewmotion.akka" %% "akka-rabbitmq" % "2.3"
libraryDependencies += "org.json4s" %% "json4s-native" % "3.3.0"
//libraryDependencies += 
//libraryDependencies += 

fork in run := true

/*resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += "The New Motion Public Repo" at "http://nexus.thenewmotion.com/content/groups/public/"
libraryDependencies += "com.thenewmotion.akka" %% "akka-rabbitmq" % "2.3"

libraryDependencies ++= Seq(
  "com.google.inject" % "guice" % "4.0",
  "javax.inject" % "javax.inject" % "1",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
  "org.webjars" % "bootstrap" % "3.3.4",
  "org.webjars" % "angularjs" % "1.3.15",
  "org.webjars" % "angular-ui-bootstrap" % "0.13.0",
  "org.mockito" % "mockito-core" % "1.10.19" % "test",
  "com.typesafe.akka" %% "akka-stream-experimental" % "1.0",
    "com.typesafe.akka" %% "akka-http-experimental" % "1.0",
	"com.hunorkovacs" %% "koauth" % "1.1.0",
	"org.json4s" %% "json4s-native" % "3.3.0",
	"com.thenewmotion.akka" %% "akka-rabbitmq" % "2.3")*/

