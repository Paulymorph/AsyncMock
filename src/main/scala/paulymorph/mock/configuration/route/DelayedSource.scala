package paulymorph.mock.configuration.route

import akka.NotUsed
import akka.stream.DelayOverflowStrategy
import akka.stream.scaladsl.Source
import paulymorph.mock.configuration.stub.MessageLike

import scala.concurrent.duration._

object DelayedSource {
  def create[T](events: Seq[(T, FiniteDuration)]): Source[T, NotUsed] = {
    case class Accumulator(delay: FiniteDuration, source: Source[T, NotUsed])
    events.foldLeft(Accumulator(0.millis, Source.empty[T])) { case (accumulator, (element, delay)) =>
      val newDelay = accumulator.delay + delay
      val newElementSource = Source.single(element).delay(newDelay, DelayOverflowStrategy.backpressure)
      Accumulator(newDelay, accumulator.source.concat(newElementSource))
    }.source
  }

  def createMessageLike[T <: MessageLike](events: Seq[T]): Source[T, NotUsed] = {
    create(events.map(e => (e, e.delay.getOrElse(0.millis))))
  }
}
