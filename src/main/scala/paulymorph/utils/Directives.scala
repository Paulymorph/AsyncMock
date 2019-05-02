package paulymorph.utils

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.directives.LoggingMagnet
import akka.http.scaladsl.server.{Directive0, Route, RouteResult}
import com.typesafe.scalalogging.Logger

object Directives extends akka.http.scaladsl.server.Directives {
  def cyclic(innerRoutes: Seq[Route]): Route = {
    val iter: Iterator[Route] = Iterator.continually(innerRoutes).flatten

    val cyclicRoute: Route = request => {
      val nextRoute = iter.next()
      nextRoute(request)
    }

    cyclicRoute
  }

  def logRequestResponse(logger: Logger): Directive0 = {
    logRequestResult({
      def logRequestAndResponse(req: HttpRequest)(res: RouteResult): Unit = {
        logger.info(req.toString)
        logger.info(res.toString)
      }

      LoggingMagnet(_ => logRequestAndResponse)
    })
  }
}
