package paulymorph.utils

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{FlatSpec, Matchers}

class DirectivesSpec extends FlatSpec with Matchers with ScalatestRouteTest {
  import Directives._
  "cyclic" should "cycle its routes" in {
    val numbers = (1 to 10).toList.map(_.toString)
    val route = cyclic(numbers.map(complete(_)))

    (numbers ++ numbers).foreach { expectedResult =>
      Get() ~> route ~> check {
        responseAs[String] shouldEqual expectedResult
      }
    }
  }
}
