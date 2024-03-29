import com.typesafe.sbt.packager.docker.Cmd
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.dockerCommands
import sbtbuildinfo.BuildInfoKey
import sbtbuildinfo.BuildInfoKeys.{ buildInfoKeys, buildInfoPackage }

name := "sk8s-example"
organization in ThisBuild := "me.lightspeed7"
version in ThisBuild := "0.0.1"

scalaVersion in ThisBuild := "2.12.8"
resolvers += Resolver.bintrayRepo("lightspeed7", "maven")

val sk8sV = "0.6.2"
//
// Projects
// ///////////////////////
lazy val global = project
  .in(file("."))
  .settings(settings)
  .settings(
    publishArtifact := false,
    skip in publish := true
  )
  .disablePlugins(AssemblyPlugin)
  .aggregate(
    common,
    consumer,
    metadata,
    producer,
    reader,
    //
    kubernetes
  )

lazy val common = project
  .settings(
    name := "common",
    settings,
    libraryDependencies ++= commonDependencies
  )
  .settings(testOptions in Test := Seq(Tests.Filter(harnessFilter)))
  .settings(testGrouping in Test := singleThreadedTests((definedTests in Test).value))
  //
  .disablePlugins(AssemblyPlugin)

lazy val metadata = project // Play App
  .enablePlugins(PlayScala, BuildInfoPlugin, DockerPlugin, Sk8sPlugin)
  .settings(
    name := "metadata",
    settings,
    sk8sPlayApp := true,
    sk8sVersion := sk8sV,
    publishArtifact := false,
    skip in publish := true,
    libraryDependencies ++= commonDependencies ++ playAppDeps,
    buildInfoVars(name, version, scalaVersion, sbtVersion)
  )
  .settings(dockerVars(name))
  .dependsOn(
    common % "test->test;compile->compile"
  )

lazy val reader = project // Play App
  .enablePlugins(PlayScala, BuildInfoPlugin, DockerPlugin, Sk8sPlugin)
  .settings(
    name := "reader",
    settings,
    sk8sPlayApp := true,
    sk8sVersion := sk8sV,
    publishArtifact := false,
    skip in publish := true,
    libraryDependencies ++= commonDependencies ++ playAppDeps,
    buildInfoVars(name, version, scalaVersion, sbtVersion)
  )
  .settings(dockerVars(name))
  .dependsOn(
    common % "test->test;compile->compile"
  )

lazy val producer = project // Play App
  .enablePlugins(PlayScala, BuildInfoPlugin, DockerPlugin, Sk8sPlugin)
  .settings(
    name := "producer",
    settings,
    sk8sPlayApp := true,
    sk8sVersion := sk8sV,
    publishArtifact := false,
    skip in publish := true,
    libraryDependencies ++= commonDependencies ++ playAppDeps,
    buildInfoVars(name, version, scalaVersion, sbtVersion)
  )
  .settings(dockerVars(name))
  .dependsOn(
    common % "test->test;compile->compile"
  )

lazy val consumer = project // Backend App
  .enablePlugins(JavaAppPackaging, BuildInfoPlugin, DockerPlugin, Sk8sPlugin)
  .settings(
    name := "consumer",
    settings,
    sk8sVersion := sk8sV,
    publishArtifact := false,
    skip in publish := true,
    libraryDependencies ++= commonDependencies,
    buildInfoVars(name, version, scalaVersion, sbtVersion)
  )
  .settings(dockerVars(name, backend = true))
  .dependsOn(
    common % "test->test;compile->compile"
  )

lazy val kubernetes = project
  .settings(
    name := "kubernetes",
    settings,
    libraryDependencies ++= commonDependencies
  )
  .settings(testOptions in Test := Seq(Tests.Filter(harnessFilter)))
  .settings(testGrouping in Test := singleThreadedTests((definedTests in Test).value))
  //
  .disablePlugins(AssemblyPlugin)

//
// Dependencies
// //////////////////////////
lazy val dependencies =
  new {
    val sk8sVersion = "0.5.6"
    val akkaV       = "2.5.23"
    val playV       = "2.6.23"
    val playJsonV   = "2.6.13"
    val sttpV       = "1.5.7"
    val scalatestV  = "3.0.4"

    val sk8sCore       = /*        */ "me.lightspeed7" %% "sk8s-core" /*        */ % sk8sVersion withSources ()
    val sk8sPlay       = /*        */ "me.lightspeed7" %% "sk8s-play" /*        */ % sk8sVersion withSources ()
    val sk8sKubernetes = /*  */ "me.lightspeed7"       %% "sk8s-kubernetes" /*  */ % sk8sVersion withSources ()
    val sk8sSlack      = /*       */ "me.lightspeed7"  %% "sk8s-slack" /*       */ % sk8sVersion withSources ()
    val sk8sCoreTest   = /*    */ "me.lightspeed7"     %% "sk8s-core" /*        */ % sk8sVersion % "test" classifier "tests" withSources ()
    val sk8sPlayTest   = /*    */ "me.lightspeed7"     %% "sk8s-play" /*        */ % sk8sVersion % "test" classifier "tests" withSources ()

    val scalaTest     = "org.scalatest"          %% "scalatest"          % scalatestV % "test" withSources ()
    val scalaTestPlus = "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2"    % "test" withSources ()

    def playLibs: Seq[ModuleID] =
      Seq( //
        scalaTestPlus,
        "com.typesafe.play" %% "play-functional" /*  */ % playJsonV /*  */ withSources () exclude ("com.google.guava", "guava"),
        "com.typesafe.play" %% "play-guice" /*       */ % playV /*      */ withSources () exclude ("com.google.guava", "guava"), //
        "com.typesafe.play" %% "filters-helpers" /*  */ % playV /*      */ withSources () exclude ("com.google.guava", "guava"),
        "com.typesafe.play" %% "play" /*             */ % playV /*      */ withSources () exclude ("com.google.guava", "guava") exclude ("com.typesafe.akka", "akka-actor") exclude ("org.scala-lang", "scala-library"),
        "com.typesafe.play" %% "play-logback" /*     */ % playV /*      */ withSources () exclude ("com.google.guava", "guava")
      )
  }

lazy val commonDependencies = Seq(
  dependencies.sk8sCore,
  dependencies.sk8sCoreTest,
  dependencies.sk8sKubernetes,
  dependencies.scalaTest
)

lazy val playAppDeps = dependencies.playLibs ++ Seq(
  dependencies.sk8sPlay,
  dependencies.sk8sCoreTest,
  dependencies.sk8sPlayTest,
  dependencies.scalaTest,
  dependencies.scalaTestPlus
)

//
// Settings and Helpers
// //////////////////////////
lazy val settings =
commonSettings ++
//wartremoverSettings ++
scalafmtSettings

lazy val compilerOptions = Seq(
  "-deprecation", //
  "-encoding",
  "UTF-8", //
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:reflectiveCalls"
  // "-language:existentials",
  // "-language:higherKinds",
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    Resolver.jcenterRepo,
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  bintrayReleaseOnPublish in ThisBuild := false
)

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true,
    scalafmtTestOnCompile := true,
    scalafmtVersion := "1.2.0"
  )

lazy val deploymentSettings = Seq(
  publishArtifact in (Test, packageBin) := true, // Publish tests jarsproject
  publishArtifact in (Test, packageSrc) := true, // Publish tests-source jars
  publishArtifact in (Compile, packageDoc) := false, // Disable ScalaDoc generation
  publishArtifact in packageDoc := false,
  publishMavenStyle := true
)

//def publishDest: Option[Resolver] = Some("Some Realm" at "tbd")

def harnessFilter(name: String): Boolean = !(name endsWith "Harness")

def singleThreadedTests(definedTests: scala.Seq[TestDefinition]): scala.Seq[Tests.Group] =
  definedTests map { test =>
    Tests.Group(name = test.name, tests = Seq(test), runPolicy = Tests.SubProcess(ForkOptions()))
  }

def buildInfoVars(name: SettingKey[String], version: SettingKey[String], scalaVersion: SettingKey[String], sbtVersion: SettingKey[String]) = {

  import scala.sys.process._

  def commit: String = ("git rev-parse --short HEAD" !!).trim

  def branch: String = ("git rev-parse --abbrev-ref HEAD" !!).trim

  def hasUnCommitted: Boolean = ("git diff-index --quiet HEAD --" !) != 0

  def generateBuildInfo(name: BuildInfoKey, version: BuildInfoKey, scalaVersion: BuildInfoKey, sbtVersion: BuildInfoKey): Seq[BuildInfoKey] =
    Seq(name, version, scalaVersion, sbtVersion) :+ BuildInfoKey.action("buildTime") {
      System.currentTimeMillis
    } :+ BuildInfoKey.action("commit") {
      commit
    } :+ BuildInfoKey.action("branch") {
      branch
    } :+ BuildInfoKey.action("hasUnCommitted") {
      hasUnCommitted
    }

  Seq(
    buildInfoPackage := "me.lightspeed7.sk8s.example",
    buildInfoKeys := generateBuildInfo(BuildInfoKey.action("name")(name.value), version, scalaVersion, sbtVersion)
  )
}

def dockerVars(
    name: SettingKey[String],
    baseImage: String = "openjdk:11-jre-slim",
    backend: Boolean = false
) = {

  val ports = if (backend) {
    Seq(8999)
  } else {
    Seq(8999, 9000)
  }

  Seq(
    packageName in Docker := name.value,
    maintainer := "Dave Buschman",
    dockerBaseImage := baseImage,
    dockerExposedPorts := ports,
    dockerCommands += Cmd("ENV", "BACKEND_SERVER true")
  )
}
