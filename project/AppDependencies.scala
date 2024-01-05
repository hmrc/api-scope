import sbt._
import play.core.PlayVersion

object AppDependencies {

  lazy val libraryDependencies = compile ++ test

  private lazy val bootstrapVersion = "7.12.0"
  private lazy val hmrcMongoVersion = "0.74.0"
  private lazy val scalaJVersion = "2.4.2"

  private lazy val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  %  bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % hmrcMongoVersion,
    "com.beachape"            %% "enumeratum-play"            % "1.7.0"
  )

  private lazy val test = Seq(
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % hmrcMongoVersion,
    "org.scalaj"              %% "scalaj-http"                % scalaJVersion,
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % bootstrapVersion,
    "org.mockito"             %% "mockito-scala-scalatest"    % "1.17.29"       
  ).map(_ % "test, it")

}
