package paulymorph.mock.parser

import paulymorph.mock.configuration.MockConfiguration

trait StubParser extends (String => Option[MockConfiguration])
