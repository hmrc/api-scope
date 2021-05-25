import sbt._
import play.core.PlayVersion

object AppDependencies {

  lazy val libraryDependencies = compile ++ test
  lazy val dependencyOverrides = overrides

  private lazy val hmrcBootstrapPlay26Version = "2.0.0"
  private lazy val hmrcSimpleReactivemongoVersion = "7.30.0-play-26"
  private lazy val hmrcHttpMetricsVersion = "1.11.0"
  private lazy val hmrcReactiveMongoTestVersion = "4.21.0-play-26"
  private lazy val hmrcTestVersion = "3.9.0-play-26"
  private lazy val scalaJVersion = "2.4.1"
  private lazy val scalatestPlusPlayVersion = "3.1.2"
  private lazy val mockitoVersion = "2.13.0"
  private lazy val wireMockVersion = "2.21.0"
  // we need to override the akka version for now as newer versions are not compatible with reactivemongo
  private lazy val akkaVersion = "2.5.23"
  private lazy val akkaHttpVersion = "10.0.15"

  private lazy val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-play-26" % hmrcBootstrapPlay26Version,
    "uk.gov.hmrc" %% "simple-reactivemongo" % hmrcSimpleReactivemongoVersion,
    "uk.gov.hmrc" %% "http-metrics" % hmrcHttpMetricsVersion,
    "com.beachape" %% "enumeratum-play" % "1.5.13"
  )

  private lazy val test = Seq(
    "uk.gov.hmrc" %% "reactivemongo-test" % hmrcReactiveMongoTestVersion % "test,it",
    "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % "test,it",
    "org.scalaj" %% "scalaj-http" % scalaJVersion % "test,it",
    "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % "test,it",
    "org.mockito" %% "mockito-scala-scalatest" % "1.7.1" % "test, it",
    "com.typesafe.play" %% "play-test" % PlayVersion.current % "test,it",
    "com.github.tomakehurst" % "wiremock-jre8" % wireMockVersion % "test,it"
  )

  private lazy val overrides = Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-protobuf" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion
  )
}