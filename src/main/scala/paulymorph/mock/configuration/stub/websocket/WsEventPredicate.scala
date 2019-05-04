package paulymorph.mock.configuration.stub.websocket

import akka.http.javadsl.model.ws.TextMessage
import akka.http.scaladsl.model.ws.Message
import io.circe.Json
import paulymorph.utils.JsonComparator

sealed trait WsEventPredicate {
  def apply(actualMessage: Message): Boolean
}

sealed private[websocket] trait JsonMessagePredicate extends WsEventPredicate {
  def comparator: (String, String) => Boolean
  def message: Json
  override def apply(actualMessage: Message): Boolean = actualMessage match {
    case textMessage: TextMessage =>
      JsonComparator.compare(textMessage.getStrictText, message, comparator)
    case _ => ???
  }
}

case class WsEquals(message: Json) extends JsonMessagePredicate {
  override val comparator: (String, String) => Boolean = _ == _
}

case class WsContains(message: Json) extends JsonMessagePredicate {
  override val comparator: (String, String) => Boolean = _.contains(_)
}

case class WsStartsWith(message: Json) extends JsonMessagePredicate {
  override val comparator: (String, String) => Boolean = _.startsWith(_)
}

sealed trait CompoundWsPredicate extends WsEventPredicate {
  def predicates: Set[WsEventPredicate]
}

case class Or(predicates: Set[WsEventPredicate]) extends CompoundWsPredicate {
  override def apply(actualMessage: Message): Boolean =
    predicates.foldLeft(true) { case (acc, predicate) =>
        acc || predicate(actualMessage)
    }
}
case class And(predicates: Set[WsEventPredicate]) extends CompoundWsPredicate {
  override def apply(actualMessage: Message): Boolean =
    predicates.foldLeft(true) { case (acc, predicate) =>
      acc && predicate(actualMessage)
    }
}