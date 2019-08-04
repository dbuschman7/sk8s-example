package me.lightspeed7.sk8s

import me.lightspeed7.sk8s.manifests.Common.Java11
import me.lightspeed7.sk8s.manifests.{ Deployment, Sk8sAppConfig, Sk8sConfigMap }
import play.api.libs.json.{ JsValue, Json }

class ManifestGeneratorTest extends Sk8sFunSuite {

  val namespaces: Seq[String] =
    Seq("default", "data", "operations")

  val params: Map[String, Map[String, String]] =
    Map(
      ("default", Map("foo"    -> "bar")),
      ("operations", Map("foo" -> "bar", "bar" -> "baz")),
      ("data", Map())
    )

  val appNamespace = "default"

  def writeDeployment(name: String, config: Sk8sAppConfig): Unit = {
    println("****************")
    Sk8sAppConfig.defaultVars(config).foreach(println)
    println("****************")
    config.envVars.foreach(println)
    println("****************")

    config.createDeploymentBase.map { dep: Deployment =>
      val json: JsValue = Json.toJson(dep)
      val pretty        = Json.prettyPrint(json)
      writeLibraryTestFile("kubernetes", "03-deployments", s"$name-deploy.json").write(pretty)
    }
  }

  test("generate namespaces") {
    namespaces
      .filterNot(_ == "default")
      .foreach { ns =>
        val yaml =
          s"""apiVersion: v1
             |kind: Namespace
             |metadata:
             |  name: $ns
         """.stripMargin

        writeLibraryTestFile("kubernetes", "01-namespaces", s"$ns-namespace.yaml").write(yaml)
      }
  }

  test("generate config map") {
    namespaces.foreach { ns =>
      val generated: String =
        Sk8sConfigMap
          .generateSk8sConfig("cluster", ns, RunMode.Developer, params.getOrElse(ns, Map.empty))

      writeLibraryTestFile("kubernetes", "02-config", s"sk8s-config-$ns.yaml").write(generated)
    }
  }

  test("metadata app") {

    val name = "metadata"

    implicit val config: Sk8sAppConfig = Sk8sAppConfig
      .create(Java11, name, "default")
      .replicas(1)
      .semVer(0, 0, 1)
      .image(DockerImage("docker.io", None, name, Some("0.0.1")))
      .cpu(0.2)
      .memory(512)
      .isApiApp
      .withEnvVar("BACKEND_SERVER", "true")
      .withEnvVar("FOO", "bar")
      .withNoServiceAccountToken

    writeDeployment(name, config)
  }

  test("reader app") {

    val name = "reader"

    implicit val config: Sk8sAppConfig = Sk8sAppConfig
      .create(Java11, name, "default")
      .replicas(1)
      .semVer(0, 0, 1)
      .image(DockerImage("docker.io", None, name, Some("0.0.1")))
      .cpu(0.2)
      .memory(512)
      .isApiApp
      .withEnvVar("BACKEND_SERVER", "true")
      .withNoServiceAccountToken

    writeDeployment(name, config)
  }

  test("producer app") {

    val name = "producer"

    implicit val config: Sk8sAppConfig = Sk8sAppConfig
      .create(Java11, name, "default")
      .replicas(1)
      .semVer(0, 0, 1)
      .image(DockerImage("docker.io", None, name, Some("0.0.1")))
      .cpu(0.2)
      .memory(512)
      .isApiApp
      .withEnvVar("BACKEND_SERVER", "true")
      .withNoServiceAccountToken

    writeDeployment(name, config)
  }

  test("consumer app") {

    val name = "consumer"

    implicit val config: Sk8sAppConfig = Sk8sAppConfig
      .create(Java11, name, "default")
      .replicas(1)
      .semVer(0, 0, 1)
      .image(DockerImage("docker.io", None, name, Some("0.0.1")))
      .cpu(0.2)
      .memory(512)
      .isBackendApp
      .withEnvVar("BACKEND_SERVER", "true")
      .withNoServiceAccountToken

    writeDeployment(name, config)
  }
}
