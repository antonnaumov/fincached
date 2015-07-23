

organization := "GitHub"

name := "FinCached"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.6"

mainClass in Compile := Some("com.github.antonaumov.fincached.Launcher")

resolvers ++= Seq(
 	"Maven Central Server"          at "http://repo1.maven.org/maven2",
  "TypeSafe Repository Releases"  at "http://repo.typesafe.com/typesafe/releases/",
  "TypeSafe Repository Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Twitter"                       at "http://maven.twttr.com"
)

libraryDependencies ++= Seq(
  "com.typesafe"                  %  "config"                         % "1.2.1",
  "spy"                           %  "spymemcached"                   % "2.8.9",
  "com.twitter.finatra"           %%  "finatra-http"                  % "2.0.0.M2",
  "com.twitter.finatra"           %%  "finatra-logback"               % "2.0.0.M2"
)

parallelExecution in Test := false

mainClass in assembly := Some("com.github.antonaumov.fincached.Launcher")

assemblyMergeStrategy in assembly := {
  case "pom.xml" | "pom.properties"        => MergeStrategy.discard
  case PathList("META-INF", xs @ _*)       => MergeStrategy.discard
  case x                                   => MergeStrategy.first
}