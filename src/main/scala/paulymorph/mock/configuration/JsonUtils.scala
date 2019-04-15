package paulymorph.mock.configuration
import cats.syntax.functor._
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor}
import paulymorph.mock.configuration.stub.{All, SseEventsResponse, _}

import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Try}

object JsonUtils {
  implicit lazy val mockConfigurationEncoder: Encoder[MockConfiguration] = Encoder.instance {
    case configuration: StubConfiguration => configuration.asJson
  }
  implicit lazy val mockConfigurationDecoder: Decoder[MockConfiguration] =
    List[Decoder[MockConfiguration]](
      Decoder[StubConfiguration].widen
    ).reduceLeft(_ or _)

  implicit lazy val stubConfigurationEncoder: Encoder[StubConfiguration] = deriveEncoder
  implicit lazy val stubConfigurationDecoder: Decoder[StubConfiguration] = deriveDecoder

  implicit lazy val stubEncoder: Encoder[Stub] = Encoder.instance {
    case responseStub: ResponseStub => responseStub.asJson
  }
  implicit lazy val stubDecoder: Decoder[Stub] =
    List[Decoder[Stub]](
      Decoder[ResponseStub].widen
    ).reduceLeft(_ or _)

  implicit lazy val responseStubEncoder: Encoder[ResponseStub] = deriveEncoder
  implicit lazy val responseStubDecoder: Decoder[ResponseStub] = deriveDecoder

  implicit lazy val predicateEncoder: Encoder[Predicate] = Encoder.instance {
    case All => All.asJson
  }
  implicit lazy val predicateDecoder: Decoder[Predicate] =
    List[Decoder[Predicate]](
      Decoder[All.type].widen
    ).reduceLeft(_ or _)

  implicit lazy val allEncoder: Encoder[All.type] = deriveEncoder
  implicit lazy val allDecoder: Decoder[All.type] = deriveDecoder

  implicit lazy val responseEncoder: Encoder[Response] = Encoder.instance {
    case sseResponse: SseEventsResponse => sseResponse.asJson
    case wsResponse: WebSocketEventsResponse => wsResponse.asJson
  }
  implicit lazy val responseDecoder: Decoder[Response] =
    List[Decoder[Response]](
      Decoder[SseEventsResponse].widen,
      Decoder[WebSocketEventsResponse].widen
    ).reduceLeft(_ or _)


  implicit lazy val sseEventsResponseEncoder: Encoder[SseEventsResponse] =
    Encoder.forProduct3("events", "timeout", "type") { resp =>
      (resp.events, resp.timeout, "sse")
    }
  implicit lazy val sseEventsResponseDecoder: Decoder[SseEventsResponse] =
    (c: HCursor) =>
      for {
        responseType <- c.downField("type").as[String]
        events <- c.downField("events").as[Seq[SseMessage]]
        timeout <- c.downField("timeout").as[Option[FiniteDuration]]
        result <- responseType match {
          case "sse" => Right(SseEventsResponse(events, timeout))
          case _ => Left(DecodingFailure(s"$responseType is not sse!", c.history))
        }
      } yield result

  implicit lazy val sseEventEncoder: Encoder[SseMessage] = deriveEncoder
  implicit lazy val sseEventDecoder: Decoder[SseMessage] = deriveDecoder

  implicit lazy val wsEventsResponseEncoder: Encoder[WebSocketEventsResponse] =
    Encoder.forProduct3("events", "timeout", "type") { resp =>
      (resp.events, resp.timeout, "websocket")
    }
  implicit lazy val wsEventsResponseDecoder: Decoder[WebSocketEventsResponse] =
    (c: HCursor) =>
      for {
        responseType <- c.downField("type").as[String]
        events <- c.downField("events").as[Seq[WsMessage]]
        timeout <- c.downField("timeout").as[Option[FiniteDuration]]
        result <- responseType match {
          case "websocket" => Right(WebSocketEventsResponse(events, timeout))
          case _ => Left(DecodingFailure(s"$responseType is not websocket!", c.history))
        }
      } yield result

  implicit lazy val wsEventEncoder: Encoder[WsMessage] = deriveEncoder
  implicit lazy val wsEventDecoder: Decoder[WsMessage] = deriveDecoder

  implicit lazy val finiteDurationEncoder: Encoder[FiniteDuration] =
    Encoder.encodeString.contramap(time => s"${time.toMillis} millis")
  implicit lazy val finiteDurationDecoder: Decoder[FiniteDuration] =
    Decoder.decodeString.emapTry(string =>
        string.split(" ", 2) match {
          case Array(amountString, unitString) =>
            Try(FiniteDuration(amountString.toLong, unitString))
          case _ => Failure(new IllegalArgumentException(s"$string is not time!"))
        }
    )
}
