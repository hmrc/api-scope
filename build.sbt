import play.core.PlayVersion
import play.sbt.PlayScala
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

bloopAggregateSourceDependencies in Global := true

lazy val appName = "api-scope"

lazy val playSettings: Seq[Setting[_]] = Seq.empty

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .settings(playSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(ScoverageSettings(): _*)
  .settings(
    majorVersion := 0,
    targetJvm := "jvm-1.8",
    scalaVersion := "2.12.12",
    libraryDependencies ++= AppDependencies.libraryDependencies,
    dependencyOverrides ++= AppDependencies.dependencyOverrides,
    retrieveManaged := true
  )
  .settings(
    addTestReportOption(Test, "test-reports"),
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-eT"),
    Test / fork := false,
    Test / unmanagedSourceDirectories ++= Seq(
      baseDirectory.value / "test",
      baseDirectory.value / "test-common"
    ),
    Test / parallelExecution := false
  )
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-eT"),
    IntegrationTest / fork := false,
    IntegrationTest / unmanagedSourceDirectories ++= Seq(
      baseDirectory.value / "integration",
      baseDirectory.value / "test-common"
    ),
    IntegrationTest / parallelExecution := false
  )
