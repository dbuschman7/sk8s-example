package controllers

import javax.inject.Inject
import me.lightspeed7.sk8s.JsonResult.ok
import me.lightspeed7.sk8s.{ ApplicationInfo, Sk8s, Sk8sContext }
import play.api.i18n.I18nSupport
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc._

class Application @Inject()(
    context: Sk8sContext,
    components: ControllerComponents //
) extends AbstractController(components)
    with I18nSupport {

  import context._

  val info = ApplicationInfo("App is running", appInfo.appName, appInfo.version, "Info", appInfo.startTime)

  val obj: JsValue = Json.toJson(info)

  def index: Action[AnyContent] = Action { _ =>
    ok(obj).toResult
  }

}
