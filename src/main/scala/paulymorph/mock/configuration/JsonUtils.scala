package paulymorph.mock.configuration
import akka.http.scaladsl.model.sse.ServerSentEvent
import cats.syntax.functor._
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import paulymorph.mock.configuration.stub.{All, SseEventsResponse, _}

object JsonUtils {
  implicit lazy val mockConfigurationEncoder: Encoder[MockConfiguration] = Encoder.instance {
    case configuration: StubConfiguration => configuration.asJson
  }
  implicit lazy val stubConfigurationEncoder: Encoder[StubConfiguration] = deriveEncoder

  implicit lazy val stubEncoder: Encoder[Stub] = Encoder.instance {
    case responseStub: ResponseStub => responseStub.asJson
  }
  implicit lazy val responseStubEncoder: Encoder[ResponseStub] = deriveEncoder

  implicit lazy val predicateEncoder: Encoder[Predicate] = Encoder.instance {
    case All => All.asJson
  }
  implicit lazy val allEncoder: Encoder[All.type] = deriveEncoder

  implicit lazy val responseEncoder: Encoder[Response] = Encoder.instance {
    case sseResponse: SseEventsResponse => sseResponse.asJson
    case wsResponse: WebSocketEventsResponse => wsResponse.asJson
  }
  implicit lazy val sseEventsResponseEncoder: Encoder[SseEventsResponse] = deriveEncoder
  implicit lazy val sseEventEncoder: Encoder[ServerSentEvent] = deriveEncoder

  implicit lazy val wsEventsResponseEncoder: Encoder[WebSocketEventsResponse] = deriveEncoder

  implicit lazy val mockConfigurationDecoder: Decoder[MockConfiguration] =
    List[Decoder[MockConfiguration]](
      Decoder[StubConfiguration].widen
    ).reduceLeft(_ or _)
  implicit lazy val stubConfigurationDecoder: Decoder[StubConfiguration] = deriveDecoder

  implicit lazy val stubDecoder: Decoder[Stub] =
    List[Decoder[Stub]](
      Decoder[ResponseStub].widen
    ).reduceLeft(_ or _)
  implicit lazy val responseStubDecoder: Decoder[ResponseStub] = deriveDecoder

  implicit lazy val predicateDecoder: Decoder[Predicate] =
    List[Decoder[Predicate]](
      Decoder[All.type].widen
    ).reduceLeft(_ or _)

  implicit lazy val allDecoder: Decoder[All.type] = deriveDecoder

  implicit lazy val responseDecoder: Decoder[Response] =
    List[Decoder[Response]](
      Decoder[SseEventsResponse].widen,
      Decoder[WebSocketEventsResponse].widen
    ).reduceLeft(_ or _)

  implicit lazy val sseEventsResponseDecoder: Decoder[SseEventsResponse] = deriveDecoder
  implicit lazy val sseEventDecoder: Decoder[ServerSentEvent] = deriveDecoder
  implicit lazy val wsEventsResponseDecoder: Decoder[WebSocketEventsResponse] = deriveDecoder
}
