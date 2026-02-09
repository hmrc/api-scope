import uk.gov.hmrc.DefaultBuildSettings

lazy val appName = "api-scope"

Global / bloopAggregateSourceDependencies := true
Global / bloopExportJarClassifiers := Some(Set("sources"))

inThisBuild(
  List(
    scalaVersion := "3.7.4",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)
ThisBuild / majorVersion := 0

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(ScoverageSettings())
  .settings(
    libraryDependencies ++= AppDependencies.libraryDependencies,
    retrieveManaged := true
  )
  .settings(
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-eT"),
    Test / unmanagedSourceDirectories += baseDirectory.value / "test-common"
  )
  // .settings(
  //   scalacOptions ++= Seq(
  //     "-Wconf:cat=unused&src=views/.*\\.scala:s",
  //     "-Wconf:cat=unused&src=.*RoutesPrefix\\.scala:s",
  //     "-Wconf:cat=unused&src=.*Routes\\.scala:s",
  //     "-Wconf:cat=unused&src=.*ReverseRoutes\\.scala:s"
  //   )
  // )

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    Test / testOptions := Seq(Tests.Argument(TestFrameworks.ScalaTest, "-eT")),
    Test / unmanagedSourceDirectories += baseDirectory.value / "testcommon"
  )

commands ++= Seq(
  Command.command("cleanAll") { state => "clean" :: "it/clean" :: state },
  Command.command("fmtAll") { state => "scalafmtAll" :: "it/scalafmtAll" :: state },
  Command.command("fixAll") { state => "scalafixAll" :: "it/scalafixAll" :: state },
  Command.command("testAll") { state => "test" :: "it/test" :: state },

  Command.command("run-all-tests") { state => "testAll" :: state },

  Command.command("clean-and-test") { state => "cleanAll" :: "compile" :: "run-all-tests" :: state },

  // Coverage does not need compile !
  Command.command("pre-commit") { state => "cleanAll" :: "fmtAll" :: "fixAll" :: "coverage" :: "testAll" :: "coverageOff" :: "coverageAggregate" :: state }
)
