import sbt._
import play.core.PlayVersion

object AppDependencies {

  lazy val libraryDependencies = compile ++ test

  private lazy val bootstrapVersion = "9.0.0"
  private lazy val hmrcMongoVersion = "1.7.0"
  private lazy val scalaJVersion    = "2.4.2"
  val commonDomainVersion           = "0.13.0"

  private lazy val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "api-platform-common-domain" % commonDomainVersion
  )

  private lazy val test = Seq(
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"         % hmrcMongoVersion,
    "org.scalaj"        %% "scalaj-http"                     % scalaJVersion,
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"          % bootstrapVersion,
    "org.mockito"       %% "mockito-scala-scalatest"         % "1.17.29",
    "uk.gov.hmrc"       %% "api-platform-test-common-domain" % commonDomainVersion
  ).map(_ % "test")

}
