import _root_.play.core.PlayVersion
import _root_.play.routes.compiler.StaticRoutesGenerator
import _root_.play.sbt.PlayImport._
import _root_.play.sbt.PlayScala
import _root_.play.sbt.routes.RoutesKeys.routesGenerator
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

lazy val appName = "api-scope"

lazy val appDependencies: Seq[ModuleID] = compile ++ test

lazy val hmrcMicroserviceBootstrapVersion = "8.3.0"
lazy val hmrcPlayReactivemongoVersion = "6.2.0"
lazy val hmrcHttpMetricsVersion = "1.2.0"
lazy val hmrcReactiveMongoTestVersion = "3.1.0"
lazy val hmrcTestVersion = "3.1.0"
lazy val scalaJVersion = "2.4.0"
lazy val scalaTestVersion = "3.0.4"
lazy val scalatestPlusPlayVersion = "2.0.1"
lazy val mockitoVersion = "1.10.19"
lazy val wireMockVersion = "2.18.0"

lazy val compile = Seq(
  ws,
  "uk.gov.hmrc" %% "microservice-bootstrap" % hmrcMicroserviceBootstrapVersion,
  "uk.gov.hmrc" %% "play-reactivemongo" % hmrcPlayReactivemongoVersion,
  "uk.gov.hmrc" %% "http-metrics" % hmrcHttpMetricsVersion
)

lazy val test = Seq(
  "uk.gov.hmrc" %% "reactivemongo-test" % hmrcReactiveMongoTestVersion % "test,it",
  "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % "test,it",
  "org.scalaj" %% "scalaj-http" % scalaJVersion % "test,it",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test,it",
  "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % "test,it",
  "org.mockito" % "mockito-core" % mockitoVersion % "test,it",
  "com.typesafe.play" %% "play-test" % PlayVersion.current % "test,it",
  "com.github.tomakehurst" % "wiremock" % wireMockVersion % "test,it"
)


lazy val playSettings: Seq[Setting[_]] = Seq.empty

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
  .settings(playSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    majorVersion := 0,
    targetJvm := "jvm-1.8",
    scalaVersion := "2.11.11",
    libraryDependencies ++= appDependencies,
    parallelExecution in Test := false,
    fork in Test := false,
    retrieveManaged := true,
    routesGenerator := StaticRoutesGenerator
  )
  .settings(inConfig(TemplateTest)(Defaults.testSettings): _*)
  .settings(testOptions in Test := Seq(Tests.Filter(unitFilter)),
    addTestReportOption(Test, "test-reports")
  )
  .configs(IntegrationTest)
  .settings(inConfig(TemplateItTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest := false,
    unmanagedSourceDirectories in IntegrationTest := Seq((baseDirectory in IntegrationTest).value / "test/it" ),
    addTestReportOption(IntegrationTest, "int-test-reports"),
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest := false)
  .settings(
    resolvers += Resolver.bintrayRepo("hmrc", "releases"),
    resolvers += Resolver.jcenterRepo)
  .settings(ivyScala := ivyScala.value map {
    _.copy(overrideScalaVersion = true)
  })

lazy val allPhases = "tt->test;test->test;test->compile;compile->compile"
lazy val allItPhases = "tit->it;it->it;it->compile;compile->compile"

lazy val TemplateTest = config("tt") extend Test
lazy val TemplateItTest = config("tit") extend IntegrationTest

def unitFilter(name: String): Boolean = name startsWith "unit"

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
  tests map {
    test => Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
  }

// Coverage configuration
coverageMinimum := 86
coverageFailOnMinimum := true
coverageExcludedPackages := "<empty>;com.kenshoo.play.metrics.*;.*definition.*;prod.*;testOnlyDoNotUseInAppConf.*;app.*;uk.gov.hmrc.BuildInfo;uk.gov.hmrc.config.*"
