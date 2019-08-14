package me.lightspeed7.sk8s.example

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import com.typesafe.scalalogging.StrictLogging
import me.lightspeed7.sk8s._
import me.lightspeed7.sk8s.telemetry.TelemetryRegistry
import me.lightspeed7.sk8s.util.AutoClose
import org.joda.time.DateTime

object Application extends App with StrictLogging {

  implicit val appInfo: AppInfo = sk8s.build.appInfo

  for (app <- AutoClose(new BackendApplication(appInfo))) {

    import app._

    logger.info(s"Beginning app '${app.appInfo.appName}'")
    try {
      // Stand up the application internals
      //
      val _ =
        Variables.source(Sources.env, "MY_POD_IP", Constant("unknown")).value
      val counter = TelemetryRegistry.counter("david")(app.appInfo)

      // Standup Completed
      logger.info(banner)

      Variables.logConfig(logger.underlying)

      // Add code here

      runUntilStopped() // daemon mode if desired
      //
    } catch {
      case ex: Throwable => shutdown(ex)
    } finally {
      logger.debug("***********************************************************")
      logger.debug("** Shutdown")
      logger.debug("***********************************************************")
    }

  }

  def banner: String = // http://patorjk.com/software/taag/#p=display&f=Big%20Money-sw&t=Type%20App%20Name
    """
      |  ______
      | /      \
      |/$$$$$$  |  ______   _______    _______  __    __  _____  ____    ______    ______
      |$$ |  $$/  /      \ /       \  /       |/  |  /  |/     \/    \  /      \  /      \
      |$$ |      /$$$$$$  |$$$$$$$  |/$$$$$$$/ $$ |  $$ |$$$$$$ $$$$  |/$$$$$$  |/$$$$$$  |
      |$$ |   __ $$ |  $$ |$$ |  $$ |$$      \ $$ |  $$ |$$ | $$ | $$ |$$    $$ |$$ |  $$/
      |$$ \__/  |$$ \__$$ |$$ |  $$ | $$$$$$  |$$ \__$$ |$$ | $$ | $$ |$$$$$$$$/ $$ |
      |$$    $$/ $$    $$/ $$ |  $$ |/     $$/ $$    $$/ $$ | $$ | $$ |$$       |$$ |
      | $$$$$$/   $$$$$$/  $$/   $$/ $$$$$$$/   $$$$$$/  $$/  $$/  $$/  $$$$$$$/ $$/
      |
      |""".stripMargin
}
