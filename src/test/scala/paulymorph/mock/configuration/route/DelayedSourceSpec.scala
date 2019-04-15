package paulymorph.mock.configuration.route
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.testkit.TestProbe

import scala.concurrent.duration._

class DelayedSourceSpec extends org.scalatest.FlatSpec {
  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()

  "DelayedSource" should "delay one element" in {
    val source = DelayedSource.create(Seq((1, 1.second)))
    val probe = TestProbe()
    source.to(Sink.actorRef(probe.ref, "completed")).run()
    probe.expectNoMessage(0.5.seconds)
    probe.expectMsg(2.seconds, 1)
    probe.expectMsg("completed")
  }

  it should "delay several elements" in {
    val source = DelayedSource.create(Seq((1, 0.second), (2, 1.second)))
    val probe = TestProbe()
    source.to(Sink.actorRef(probe.ref, "completed")).run()
    probe.expectMsg(2.seconds, 1)
    probe.expectNoMessage(0.5.seconds)
    probe.expectMsg(2.seconds, 2)
    probe.expectMsg("completed")
  }

  it should "source messages in a correct order" in {
    val source = DelayedSource.create(Seq((1, 1.second), (2, 0.second), (3, 0.second), (4, 1.second)))
    val probe = TestProbe()
    source.to(Sink.actorRef(probe.ref, "completed")).run()
    probe.expectNoMessage(0.5.seconds)
    probe.expectMsg(2.seconds, 1)
    probe.expectMsg(2)
    probe.expectMsg(3)
    probe.expectNoMessage(0.5.seconds)
    probe.expectMsg(2.seconds, 4)
    probe.expectMsg("completed")
  }
}
