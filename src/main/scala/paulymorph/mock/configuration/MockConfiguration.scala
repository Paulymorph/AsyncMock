package paulymorph.mock.configuration

import paulymorph.mock.configuration.stub.Stub

sealed trait MockConfiguration {
  def port: Int
}

case class StubConfiguration(port: Int, stubs: Seq[Stub]) extends MockConfiguration