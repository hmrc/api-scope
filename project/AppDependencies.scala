import sbt._
import play.core.PlayVersion

object AppDependencies {

  lazy val libraryDependencies = compile ++ test
  lazy val dependencyOverrides = overrides

  private lazy val hmrcBootstrapPlay27Version = "5.8.0"
  private lazy val hmrcSimpleReactivemongoVersion = "8.0.0-play-27"
  private lazy val hmrcReactiveMongoTestVersion = "5.0.0-play-27"
  private lazy val scalaJVersion = "2.4.1"
  private lazy val scalatestPlusPlayVersion = "4.0.0"
  // we need to override the akka version for now as newer versions are not compatible with reactivemongo
  private lazy val akkaVersion = "2.6.1"

  private lazy val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-27" % hmrcBootstrapPlay27Version,
    "uk.gov.hmrc" %% "simple-reactivemongo" % hmrcSimpleReactivemongoVersion,
    "uk.gov.hmrc" %% "http-metrics" % "2.1.0-play-27",
    "com.beachape" %% "enumeratum-play" % "1.5.13"
  )

  private lazy val test = Seq(
    "uk.gov.hmrc" %% "reactivemongo-test" % hmrcReactiveMongoTestVersion % "test,it",
    "org.scalaj" %% "scalaj-http" % scalaJVersion % "test,it",
    "uk.gov.hmrc" %% "bootstrap-test-play-27" % hmrcBootstrapPlay27Version % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % "test,it",
    "org.mockito" %% "mockito-scala-scalatest" % "1.7.1" % "test, it",
    "com.typesafe.play" %% "play-test" % PlayVersion.current % "test,it",
    "org.pegdown" % "pegdown" % "1.6.0" % "test, it"
  )

  private lazy val overrides = Seq(
    "com.typesafe.akka" %% "akka-protobuf" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion
  )
}