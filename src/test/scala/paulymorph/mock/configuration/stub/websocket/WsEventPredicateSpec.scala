package paulymorph.mock.configuration.stub.websocket

import akka.http.scaladsl.model.ws.TextMessage
import io.circe.Json
import io.circe.syntax._
import org.scalatest.{FlatSpec, Matchers}

class WsEventPredicateSpec extends FlatSpec with Matchers {
  val `{"foo": "bar"}`: Json = Map("foo"->"bar").asJson
  val `"foo"`: Json = "foo".asJson

  "WsEquals" should "check messages on equality" in {
    WsEquals(`{"foo": "bar"}`)(TextMessage("""{"foo": "bar"}""")) shouldBe true
    WsEquals(`{"foo": "bar"}`)(TextMessage("""{"foo": "bar", "zoo": "zar"}""")) shouldBe true
    WsEquals(`{"foo": "bar"}`)(TextMessage("""{"foos": "bar"}""")) shouldBe false
    WsEquals(`{"foo": "bar"}`)(TextMessage("""{"sfoo": "bar"}""")) shouldBe false
    WsEquals(`{"foo": "bar"}`)(TextMessage("""{"foo": "sbar"}""")) shouldBe false
    WsEquals(`{"foo": "bar"}`)(TextMessage("""{"foo": "bars"}""")) shouldBe false
    WsEquals(`{"foo": "bar"}`)(TextMessage("""{"foo": "bsar"}""")) shouldBe false
    WsEquals(`{"foo": "bar"}`)(TextMessage("foobar")) shouldBe false

    WsEquals(`"foo"`)(TextMessage("foo")) shouldBe true
    WsEquals(`"foo"`)(TextMessage("food")) shouldBe false
    WsEquals(`"foo"`)(TextMessage("dfoo")) shouldBe false
    WsEquals(`"foo"`)(TextMessage("\"foo\"")) shouldBe false
  }

  "WsContains" should "check messages on containment" in {
    WsContains(`{"foo": "bar"}`)(TextMessage("""{"foo": "bar"}""")) shouldBe true
    WsContains(`{"foo": "bar"}`)(TextMessage("""{"foo": "bar", "zoo": "zar"}""")) shouldBe true
    WsContains(`{"foo": "bar"}`)(TextMessage("""{"foos": "bar"}""")) shouldBe false
    WsContains(`{"foo": "bar"}`)(TextMessage("""{"sfoo": "bar"}""")) shouldBe false
    WsContains(`{"foo": "bar"}`)(TextMessage("""{"foo": "sbar"}""")) shouldBe true
    WsContains(`{"foo": "bar"}`)(TextMessage("""{"foo": "bars"}""")) shouldBe true
    WsContains(`{"foo": "bar"}`)(TextMessage("""{"foo": "bsar"}""")) shouldBe false
    WsContains(`{"foo": "bar"}`)(TextMessage("foobar")) shouldBe false

    WsContains(`"foo"`)(TextMessage("foo")) shouldBe true
    WsContains(`"foo"`)(TextMessage("food")) shouldBe true
    WsContains(`"foo"`)(TextMessage("dfoo")) shouldBe true
    WsContains(`"foo"`)(TextMessage("\"foo\"")) shouldBe true
    WsContains(`"foo"`)(TextMessage("fsoo")) shouldBe false
  }
}
