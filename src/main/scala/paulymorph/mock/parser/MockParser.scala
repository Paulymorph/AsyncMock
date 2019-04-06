package paulymorph.mock.parser

import paulymorph.mock.configuration.MockConfiguration

trait MockParser extends (String => Option[MockConfiguration])
