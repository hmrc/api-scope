import sbt._
import play.core.PlayVersion

object AppDependencies {

  lazy val libraryDependencies = compile ++ test

  private lazy val bootstrapVersion = "6.2.0"
  private lazy val hmrcMongoVersion = "0.68.0"
  private lazy val scalaJVersion = "2.4.2"

  private lazy val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  %  bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % hmrcMongoVersion,
    "com.beachape"            %% "enumeratum-play" % "1.5.13"
  )

  private lazy val test = Seq(
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-28" % hmrcMongoVersion % "test, it",
    "org.scalaj" %% "scalaj-http" % scalaJVersion % "test,it",
    "uk.gov.hmrc"  %% "bootstrap-test-play-28"   % bootstrapVersion % "test, it",
    "org.mockito" %% "mockito-scala-scalatest" % "1.16.46" % "test, it"
  )

}
