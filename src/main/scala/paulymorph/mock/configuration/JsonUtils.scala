package paulymorph.mock.configuration

import cats.syntax.functor._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import paulymorph.mock.configuration.stub.{RequestExpectation, _}

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
    case predicate: LeafPredicate => predicate.asJson
    case predicate: CompoundPredicate => predicate.asJson
  }

  implicit lazy val predicateDecoder: Decoder[Predicate] =
    (c: HCursor) => {
      val predicates: List[Decoder.Result[Predicate]] =
        c.get[Seq[RequestExpectation]]("equals").map(e => And(e.map(Equals))) ::
          c.get[Seq[RequestExpectation]]("startsWith").map(e => And(e.map(StartsWith))) ::
          c.get[Seq[RequestExpectation]]("contains").map(e => And(e.map(Contains))) ::
          c.get[Seq[Predicate]]("or").map(Or) ::
          c.get[Seq[Predicate]]("and").map(And) ::
          Nil

      val resultPredicate = predicates.flatMap(_.toOption) match {
        case single :: Nil => single
        case predicatesList => And(predicatesList)
      }
      Right(resultPredicate)
    }

  implicit lazy val leafPredicateEncoder: Encoder[LeafPredicate] = { predicate =>
    def encodeLeaf(key: String): Json =
      Encoder.encodeMap[String, Json].apply(Map(key -> predicate.requestExpectation.asJson))

    predicate match {
      case _: Equals => encodeLeaf("equals")
      case _: StartsWith => encodeLeaf("startsWith")
      case _: Contains => encodeLeaf("contains")
    }
  }

  implicit lazy val compoundPredicateEncoder: Encoder[CompoundPredicate] = { predicate =>
    def encodeCompound(key: String): Json =
      Encoder.encodeMap[String, Json].apply(Map(key -> predicate.predicates.asJson))

    predicate match {
      case _: Or => encodeCompound("or")
      case _: And => encodeCompound("and")
    }
  }

  implicit lazy val requestExpectationsEncoder: Encoder[RequestExpectation] = deriveEncoder

  implicit lazy val requestExpectationsDecoder: Decoder[Seq[RequestExpectation]] =
    (c: HCursor) => {
      val expectations = c.get[Json]("body").map(BodyExpectation) ::
        c.get[String]("path").map(PathExpectation) ::
        c.get[String]("method").map(MethodExpectation) ::
        c.get[Map[String, String]]("query").map(QueryExpectation) ::
        Nil

      Right(expectations.flatMap(_.toOption))
    }

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
