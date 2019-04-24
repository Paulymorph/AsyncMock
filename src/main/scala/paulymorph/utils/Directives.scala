package paulymorph.utils

import akka.http.scaladsl.server.Route

object Directives extends akka.http.scaladsl.server.Directives {
  def cyclic(innerRoutes: Seq[Route]): Route = {
    val iter: Iterator[Route] = Iterator.continually(innerRoutes).flatten

    val cyclicRoute: Route = request => {
      val nextRoute = iter.next()
      nextRoute(request)
    }

    cyclicRoute
  }
}
