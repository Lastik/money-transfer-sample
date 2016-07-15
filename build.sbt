name := "money-transfer"

version := "1.0"

scalaVersion := "2.10.2"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "spray nightlies" at "http://nightlies.spray.io"

val akkaVersion = "2.3.6"
val sprayVersion = "1.3.2"
val sprayJsonVersion = "1.3.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka"  %% "akka-actor"       % akkaVersion,
  "com.typesafe.akka"  %% "akka-slf4j"       % akkaVersion,
  "ch.qos.logback"      % "logback-classic"  % "1.0.13",
  "io.spray"            %% "spray-can"        % sprayVersion,
  "io.spray"            %% "spray-routing"    % sprayVersion force(),
  "io.spray"           %% "spray-json"       % sprayJsonVersion,
  "com.novocode"        % "junit-interface"  % "0.7"          % "test->default",
  "joda-time" % "joda-time" % "2.9.4",
  "com.squants"        %% "squants"  % "0.5.3",
  "org.scalatest" %% "scalatest" % "2.2.2" % "test",
  "com.typesafe.akka"  %% "akka-testkit"     % akkaVersion        % "test",
  "io.spray"            %% "spray-testkit"    % sprayVersion % "test"
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")