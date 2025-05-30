import sbt._

object AppDependencies {

  lazy val libraryDependencies = compile ++ test

  private lazy val bootstrapVersion = "9.12.0"
  private lazy val hmrcMongoVersion = "2.6.0"
  val commonDomainVersion           = "0.18.0"

  private lazy val compile = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-backend-play-30"        % bootstrapVersion,
    "uk.gov.hmrc.mongo"             %% "hmrc-mongo-play-30"               % hmrcMongoVersion,
    "uk.gov.hmrc"                   %% "api-platform-common-domain"       % commonDomainVersion
  )

  private lazy val test = Seq(
    "uk.gov.hmrc.mongo"             %% "hmrc-mongo-test-play-30"              % hmrcMongoVersion,
    "com.softwaremill.sttp.client3" %% "core"                                 % "3.11.0",
    "uk.gov.hmrc"                   %% "bootstrap-test-play-30"               % bootstrapVersion,
    "org.mockito"                   %% "mockito-scala-scalatest"              % "1.17.45",
    "uk.gov.hmrc"                   %% "api-platform-common-domain-fixtures"  % commonDomainVersion
  ).map(_ % "test")

}
