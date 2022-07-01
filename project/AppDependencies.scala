import sbt._
import play.core.PlayVersion

object AppDependencies {

  lazy val libraryDependencies = compile ++ test
  lazy val dependencyOverrides = overrides

  private lazy val bootstrapVersion = "5.16.0"
  private lazy val hmrcMongoTestVersion = "0.64.0"
  private lazy val hmrcMongoVersion = "0.64.0"
  private lazy val scalaJVersion = "2.4.2"
  private lazy val scalatestPlusPlayVersion = "4.0.0"
  // we need to override the akka version for now as newer versions are not compatible with reactivemongo
  private lazy val akkaVersion = "2.6.14"

  private lazy val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  %  bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % hmrcMongoVersion,
    "com.beachape"            %% "enumeratum-play" % "1.5.13",
    "uk.gov.hmrc"             %% "play-json-union-formatter"  % "1.15.0-play-28"
  )

  private lazy val test = Seq(
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-28" % hmrcMongoTestVersion % "test, ]it",
    "org.scalaj" %% "scalaj-http" % scalaJVersion % "test,it",
    "uk.gov.hmrc"  %% "bootstrap-test-play-28"   % bootstrapVersion,
    "org.mockito" %% "mockito-scala-scalatest" % "1.16.46" % "test, it"
  )

  private lazy val overrides = Seq(
    "com.typesafe.akka" %% "akka-protobuf" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion
  )
}