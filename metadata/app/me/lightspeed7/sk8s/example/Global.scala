package me.lightspeed7.sk8s.example

import java.io.File
import java.nio.file.{ Path, Paths }

import javax.inject.Inject
import me.lightspeed7.sk8s._
import me.lightspeed7.sk8s.logging.LazyJsonLogging

import play.api._
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

class GlobalModule extends Sk8sBindings {

  implicit val appInfo: AppInfo = sk8s.build.appInfo

  override def configure(): Unit = {
    generate(appInfo)
    //
    bind(classOf[Initialize]).asEagerSingleton() // initialize actors
  }

}

class Initialize @Inject()( //
                           /*    */ val lifecycle: ApplicationLifecycle,
                           implicit val appCtx: Sk8sContext)
    extends LazyJsonLogging {

  def isKubernetes(basePath: Path = Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/token")): Boolean =
    new File(basePath.toString).exists()

  if (isKubernetes()) {
    logger.info(s"Kubernetes - ${Sk8s.serviceAccount().isKubernetes}")
  } else {
    logger.warn("Kubernetes NOT detected !!! ")
  }

  lifecycle.addStopHook { () =>
    import appCtx.ec
    Future {
      // add shutdown code here
    }
  }

  // stand up objects here

  // finish
  //
  // Go to : http://patorjk.com/software/taag/#p=display&f=Big%20Money-sw&t=Type%20App%20Name
  Logger.info("""
      | __       __              __                      __              __
      |/  \     /  |            /  |                    /  |            /  |
      |$$  \   /$$ |  ______   _$$ |_     ______    ____$$ |  ______   _$$ |_     ______
      |$$$  \ /$$$ | /      \ / $$   |   /      \  /    $$ | /      \ / $$   |   /      \
      |$$$$  /$$$$ |/$$$$$$  |$$$$$$/    $$$$$$  |/$$$$$$$ | $$$$$$  |$$$$$$/    $$$$$$  |
      |$$ $$ $$/$$ |$$    $$ |  $$ | __  /    $$ |$$ |  $$ | /    $$ |  $$ | __  /    $$ |
      |$$ |$$$/ $$ |$$$$$$$$/   $$ |/  |/$$$$$$$ |$$ \__$$ |/$$$$$$$ |  $$ |/  |/$$$$$$$ |
      |$$ | $/  $$ |$$       |  $$  $$/ $$    $$ |$$    $$ |$$    $$ |  $$  $$/ $$    $$ |
      |$$/      $$/  $$$$$$$/    $$$$/   $$$$$$$/  $$$$$$$/  $$$$$$$/    $$$$/   $$$$$$$/
      |
      |""".stripMargin)

  Variables.logConfig(play.api.Logger.underlyingLogger)

}
