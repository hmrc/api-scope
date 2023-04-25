import play.core.PlayVersion
import play.sbt.PlayScala
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import bloop.integrations.sbt.BloopDefaults
import uk.gov.hmrc.DefaultBuildSettings

lazy val appName = "api-scope"

scalaVersion := "2.13.8"
lazy val playSettings: Seq[Setting[_]] = Seq.empty

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .settings(playSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(ScoverageSettings(): _*)
  .settings(
    majorVersion := 0,
    libraryDependencies ++= AppDependencies.libraryDependencies,
    retrieveManaged := true
  )
  .settings(
    inConfig(Test)(BloopDefaults.configSettings),
    addTestReportOption(Test, "test-reports"),
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-eT"),
    Test / fork := false,
    Test / unmanagedSourceDirectories ++= Seq(
      baseDirectory.value / "test",
      baseDirectory.value / "test-common"
    ),
    Test / parallelExecution := false
  )
  .settings(DefaultBuildSettings.integrationTestSettings())
  .configs(IntegrationTest)
  .settings(
    inConfig(IntegrationTest)(scalafixConfigSettings(IntegrationTest)),
    inConfig(IntegrationTest)(BloopDefaults.configSettings),
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-eT"),
    IntegrationTest / fork := false,
    IntegrationTest / unmanagedSourceDirectories ++= Seq(
      baseDirectory.value / "integration",
      baseDirectory.value / "test-common"
    ),
    IntegrationTest / parallelExecution := false
  )
  .settings(
    scalacOptions ++= Seq(
      "-Wconf:cat=unused&src=views/.*\\.scala:s",
      "-Wconf:cat=unused&src=.*RoutesPrefix\\.scala:s",
      "-Wconf:cat=unused&src=.*Routes\\.scala:s",
      "-Wconf:cat=unused&src=.*ReverseRoutes\\.scala:s"
    )
  )