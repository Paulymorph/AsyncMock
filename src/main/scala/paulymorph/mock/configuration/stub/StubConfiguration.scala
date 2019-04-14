package paulymorph.mock.configuration.stub

import paulymorph.mock.configuration.MockConfiguration

case class StubConfiguration(port: Int, stubs: Seq[Stub]) extends MockConfiguration
