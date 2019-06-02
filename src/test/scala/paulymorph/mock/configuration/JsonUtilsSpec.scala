package paulymorph.mock.configuration

import io.circe.Json
import io.circe.parser.decode
import io.circe.syntax._
import org.scalatest.{FlatSpec, Matchers}
import paulymorph.mock.configuration.stub._
import paulymorph.mock.configuration.stub.websocket.WsReaction

import scala.concurrent.duration._


class JsonUtilsSpec extends FlatSpec with Matchers {

  import org.scalatest.EitherValues._
  import paulymorph.mock.configuration.json.JsonUtils._

  "JsonUtils" should "parse equals predicate" in {
    import paulymorph.mock.configuration.stub.http._
    val rawBody = """{"key": "value"}"""
    val rawPath = "/bla/sdf"
    val rawQuery = """{ "q": "q1" }"""
    val rawEquals =
      s"""
         |{
         | "equals": {
         |     "body": $rawBody,
         |     "path": "$rawPath",
         |     "query": $rawQuery,
         |     "method": "post"
         |   }
         |}
      """.stripMargin

    val expectations = BodyExpectation(decode[Json](rawBody).right.value) ::
      PathExpectation(rawPath) ::
      QueryExpectation(Map("q" -> "q1")) ::
      MethodExpectation("post") ::
      Nil

    val decodedPredicate = decode[HttpPredicate](rawEquals).right.value
    decodedPredicate match {
      case And(predicates) => predicates should contain theSameElementsAs expectations.map(Equals)
      case _ => ???
    }
  }

  it should "parse SSE mock configuration correctly" in {
    val rawConfiguration =
      """
      {
        "port": 5000,
        "stubs": [
          {
            "predicates" : [],
            "responses": [
              {
                "events": [
                  {
                    "data": {"foo": "bar"}
                  },
                  {
                    "data": [1,2,3],
                    "id": "id"
                  },
                  {
                    "data": "123",
                    "delay": "3 second",
                    "id": "1234",
                    "eventType": "second"
                  }
                ],
                "type": "sse",
                "timeout": "3 minutes"
              }
            ]
          }
        ]
      }
      """

    val decoded = decode[MockConfiguration](rawConfiguration).right.value
    decoded shouldBe StubConfiguration(
      port = 5000,
      stubs = Seq(
        ResponseStub(
          predicates = Seq.empty,
          responses = Seq(
            SseEventsResponse(
              events = Seq(
                SseMessage(
                  data = Map("foo" -> "bar").asJson,
                  delay = None,
                  eventType = None,
                  id = None
                ),
                SseMessage(
                  data = Seq(1, 2, 3).asJson,
                  delay = None,
                  eventType = None,
                  id = Some("id")
                ),
                SseMessage(
                  data = "123".asJson,
                  delay = Some(3.seconds),
                  eventType = Some("second"),
                  id = Some("1234")
                )
              ),
              timeout = Some(3.minutes)
            )
          )
        )
      )
    )
  }

  it should "parse minimal SSE mock configuration correctly" in {
    val rawConfiguration =
      """
      {
        "port": 5000,
        "stubs": [
          {
            "predicates" : [],
            "responses": [
              {
                "type": "sse"
              }
            ]
          }
        ]
      }
      """

    val decoded = decode[MockConfiguration](rawConfiguration).right.value
    decoded shouldBe StubConfiguration(
      port = 5000,
      stubs = Seq(
        ResponseStub(
          predicates = Seq.empty,
          responses = Seq(
            SseEventsResponse(
              events = Seq.empty,
              timeout = None
            )
          )
        )
      )
    )
  }

  it should "parse minimal mock configuration correctly" in {
    val rawConfiguration =
      """
      {
        "port": 5000,
        "stubs": [
          {
            "predicates": [],
            "responses": []
          }
        ]
      }
      """

    decode[MockConfiguration](rawConfiguration).right.value shouldBe
      StubConfiguration(
        port = 5000,
        stubs = Seq(
          ResponseStub(
            predicates = Seq.empty,
            responses = Seq.empty
          )
        )
      )
  }

  it should "parse WebSocket mock configuration correctly" in {
    val rawConfiguration =
      """
      {
        "port": 5000,
        "stubs": [
          {
            "predicates": [],
            "responses": [
              {
                "events": [
                  {
                    "data": {"foo": "bar"},
                    "delay": "1 second"
                  },
                  {
                    "data": [1,2,3]
                  },
                  {
                    "data": "hello"
                  }
                ],
                "type": "websocket",
                "timeout": "3 minutes"
              }
            ]
          }
        ]
      }
      """

    decode[MockConfiguration](rawConfiguration).right.value shouldBe
      StubConfiguration(
        port = 5000,
        stubs = Seq(
          ResponseStub(
            predicates = Seq.empty,
            responses = Seq(
              WebSocketEventsResponse(
                events = Seq(
                  WsMessage(
                    data = Map("foo" -> "bar").asJson,
                    delay = Some(1.second)
                  ),
                  WsMessage(
                    data = Seq(1, 2, 3).asJson,
                    delay = None
                  ),
                  WsMessage(
                    data = "hello".asJson,
                    delay = None
                  )
                ),
                timeout = Some(3.minutes),
                reactions = Seq.empty
              )
            )
          )
        )
      )
  }

  it should "parse minimal WebSocket mock configuration correctly" in {
    val rawConfiguration =
      """
      {
        "port": 5000,
        "stubs": [
          {
            "predicates": [],
            "responses": [
              {
                "type": "websocket"
              }
            ]
          }
        ]
      }
      """

    decode[MockConfiguration](rawConfiguration).right.value shouldBe
      StubConfiguration(
        port = 5000,
        stubs = Seq(
          ResponseStub(
            predicates = Seq.empty,
            responses = Seq(
              WebSocketEventsResponse(
                events = Seq.empty,
                timeout = None,
                reactions = Seq.empty
              )
            )
          )
        )
      )
  }

  it should "parse WebSocket reactions responses correctly" in {
    val rawConfiguration =
      """
      {
        "port": 5000,
        "stubs": [
          {
            "predicates": [],
            "responses": [
              {
                "reactions": [
                  {
                    "predicates": [],
                    "reaction": [
                      {
                        "data": "response",
                        "delay": "1 second"
                      },
                      {
                        "data": {"foo": "bar"}
                      },
                      {
                        "data": [1,2,3]
                      }
                    ]
                  }
                ],
                "type": "websocket"
              }
            ]
          }
        ]
      }
      """

    decode[MockConfiguration](rawConfiguration).right.value shouldBe
      StubConfiguration(
        port = 5000,
        stubs = Seq(
          ResponseStub(
            predicates = Seq.empty,
            responses = Seq(
              WebSocketEventsResponse(
                events = Seq.empty,
                timeout = None,
                reactions = Seq(
                  WsReaction(
                    predicates = Seq.empty,
                    reaction = Seq(
                      WsMessage(
                        data = "response".asJson,
                        delay = Some(1.second)
                      ),
                      WsMessage(
                        data = Map("foo" -> "bar").asJson,
                        delay = None
                      ),
                      WsMessage(
                        data = Seq(1, 2, 3).asJson,
                        delay = None
                      )
                    )
                  )
                )
              )
            )
          )
        )
      )
  }

  it should "parse WebSocket reactions predicates correctly" in {
    val rawConfiguration =
      """
      {
        "port": 5000,
        "stubs": [
          {
            "predicates": [],
            "responses": [
              {
                "reactions": [
                  {
                    "predicates": [
                      {
                        "equals": { "foo": "bar" },
                        "startsWith": "1234"
                      },
                      {
                        "contains": {"foo2": "sdf"},
                        "and": [{"startsWith": "woohoo"}],
                        "or": []
                      }
                    ],
                    "reaction": []
                  }
                ],
                "type": "websocket"
              }
            ]
          }
        ]
      }
      """

    import paulymorph.mock.configuration.stub.websocket._
    decode[MockConfiguration](rawConfiguration).right.value shouldBe
      StubConfiguration(
        port = 5000,
        stubs = Seq(
          ResponseStub(
            predicates = Seq.empty,
            responses = Seq(
              WebSocketEventsResponse(
                events = Seq.empty,
                timeout = None,
                reactions = Seq(
                  WsReaction(
                    predicates = Seq(
                      And(
                        Set(
                          WsEquals(Map("foo" -> "bar").asJson),
                          WsStartsWith("1234".asJson)
                        )
                      ),
                      And(
                        Set(
                          WsContains(Map("foo2" -> "sdf").asJson),
                          And(Set(WsStartsWith("woohoo".asJson))),
                          Or(Set.empty)
                        )
                      )
                    ),
                    reaction = Seq.empty
                  )
                )
              )
            )
          )
        )
      )
  }
}
