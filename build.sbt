import _root_.play.core.PlayVersion
import _root_.play.sbt.PlayScala
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

lazy val appName = "api-scope"

lazy val appDependencies: Seq[ModuleID] = compile ++ test

lazy val hmrcBootstrapPlay26Version = "1.3.0"
lazy val hmrcSimpleReactivemongoVersion = "7.22.0-play-26"
lazy val hmrcHttpMetricsVersion = "1.6.0-play-26"
lazy val hmrcReactiveMongoTestVersion = "4.16.0-play-26"
lazy val hmrcTestVersion = "3.9.0-play-26"
lazy val scalaJVersion = "2.4.1"
lazy val scalatestPlusPlayVersion = "3.1.2"
lazy val mockitoVersion = "1.10.19"
lazy val wireMockVersion = "2.21.0"
// we need to override the akka version for now as newer versions are not compatible with reactivemongo
lazy val akkaVersion = "2.5.23"
lazy val akkaHttpVersion = "10.0.15"

lazy val compile = Seq(
  "uk.gov.hmrc" %% "bootstrap-play-26" % hmrcBootstrapPlay26Version,
  "uk.gov.hmrc" %% "simple-reactivemongo" % hmrcSimpleReactivemongoVersion,
  "uk.gov.hmrc" %% "http-metrics" % hmrcHttpMetricsVersion,
  "com.beachape" %% "enumeratum-play" % "1.5.11"
)

lazy val test = Seq(
  "uk.gov.hmrc" %% "bootstrap-play-26" % hmrcBootstrapPlay26Version % "test,it" classifier "tests",
  "uk.gov.hmrc" %% "reactivemongo-test" % hmrcReactiveMongoTestVersion % "test,it",
  "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % "test,it",
  "org.scalaj" %% "scalaj-http" % scalaJVersion % "test,it",
  "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % "test,it",
  "org.mockito" % "mockito-core" % mockitoVersion % "test,it",
  "com.typesafe.play" %% "play-test" % PlayVersion.current % "test,it",
  "com.github.tomakehurst" % "wiremock-jre8" % wireMockVersion % "test,it"
)

lazy val overrides = Set(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-protobuf" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion
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
    dependencyOverrides ++= overrides,
    parallelExecution in Test := false,
    fork in Test := false,
    retrieveManaged := true
  )
  .settings(inConfig(TemplateTest)(Defaults.testSettings): _*)
  .settings(testOptions in Test := Seq(Tests.Filter(unitFilter)),
    addTestReportOption(Test, "test-reports")
  )
  .configs(IntegrationTest)
  .settings(inConfig(TemplateItTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest := false,
    unmanagedSourceDirectories in IntegrationTest := Seq((baseDirectory in IntegrationTest).value / "test"),
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
