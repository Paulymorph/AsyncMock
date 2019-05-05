package paulymorph.mock.configuration.stub

sealed trait MockConfiguration {
  def port: Int
}

case class StubConfiguration(port: Int, stubs: Seq[Stub]) extends MockConfiguration