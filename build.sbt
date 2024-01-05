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

scalaVersion := "2.13.12"

lazy val playSettings: Seq[Setting[_]] = Seq.empty

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .settings(playSettings: _*)
  .settings(scalaSettings: _*)
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

commands ++= Seq(
  Command.command("run-all-tests") { state => "test" :: "it:test" :: state },

  Command.command("clean-and-test") { state => "clean" :: "compile" :: "run-all-tests" :: state },

  // Coverage does not need compile !
  Command.command("pre-commit") { state => "clean" :: "scalafmtAll" :: "scalafixAll" :: "coverage" :: "run-all-tests" :: "coverageOff" :: "coverageAggregate" :: state }
)
