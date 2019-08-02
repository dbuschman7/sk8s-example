package me.lightspeed7.sk8s.example

import javax.inject.Inject
import me.lightspeed7.sk8s.{ ResponseFilter, TelemetryFilter }
import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter
import play.filters.gzip.GzipFilter

class Filters @Inject()(
    gzip: GzipFilter,
    telemetry: TelemetryFilter,
    response: ResponseFilter //
) extends HttpFilters {

  def filters: Seq[EssentialFilter] = Seq(gzip, response, telemetry)

}
