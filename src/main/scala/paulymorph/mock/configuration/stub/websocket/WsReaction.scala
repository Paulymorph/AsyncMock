package paulymorph.mock.configuration.stub.websocket

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.{Flow, Source}
import paulymorph.mock.configuration.route.DelayedSource
import paulymorph.mock.configuration.stub.WsMessage

case class WsReaction(predicates: Seq[WsEventPredicate], reaction: Seq[WsMessage])

object WsReaction {
  def toFlow(reactions: Seq[WsReaction]): Flow[Message, Message, NotUsed] = {
    Flow[Message].flatMapConcat { message =>
      reactions.find(_.predicates.forall(predicate => predicate(message)))
        .fold(Source.empty[Message]) {
          reaction => DelayedSource.createMessageLike(reaction.reaction).map(_.toWs)
        }
    }
  }
}