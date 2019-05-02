package paulymorph.utils

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.Matchers

trait BaseSpec extends Matchers {
  implicit lazy val ac = ActorSystem("test")
  implicit lazy val ec = ac.dispatcher
  implicit lazy val materializer = ActorMaterializer()
}
