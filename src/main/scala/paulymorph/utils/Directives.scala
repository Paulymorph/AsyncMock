package paulymorph.utils

import akka.http.scaladsl.server.{Directives, Route}

object Directives extends Directives {
  def cyclic(innerRoutes: Seq[Route]): Route = {
    val iter: Iterator[Route] = Iterator.continually(innerRoutes).flatten

    val cyclicRoute: Route = request => {
      val nextRoute = iter.next()
      nextRoute(request)
    }

    cyclicRoute
  }
}
