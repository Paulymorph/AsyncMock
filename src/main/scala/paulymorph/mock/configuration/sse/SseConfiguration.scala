package paulymorph.mock.configuration.sse

import paulymorph.mock.configuration.MockConfiguration

case class SseConfiguration(port: Int, stubs: Seq[Stub]) extends MockConfiguration {
  val protocol = "sse"
}
