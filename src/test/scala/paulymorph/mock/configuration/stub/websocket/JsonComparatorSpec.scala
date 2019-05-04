package paulymorph.mock.configuration.stub.websocket

import io.circe.Json
import io.circe.parser.decode
import org.scalatest.{FlatSpec, Matchers}
import paulymorph.utils.JsonComparator

class JsonComparatorSpec extends FlatSpec with Matchers {
  val equals = (actual: String, expected: String) => actual == expected

  it should "compare strings with comparator" in {
    val expected = decode[Json]("\"bla\"").getOrElse(???)
    JsonComparator.compare("bla", expected, equals) shouldBe true
    JsonComparator.compare("blas", expected, equals) shouldBe false
    JsonComparator.compare("\"bla\"", expected, equals) shouldBe false
  }

  it should "compare objects correctly" in {
    val expected = decode[Json](""" {"foo": "bar"} """).getOrElse(???)
    JsonComparator.compare(""" {"foo" : "bar"} """, expected, equals) shouldBe true
    JsonComparator.compare(""" {"foo" : "bar", "unfoo": "unbar"} """, expected, equals) shouldBe true
    JsonComparator.compare(""" {"foo" : {}} """, expected, equals) shouldBe false
    JsonComparator.compare(""" {"foo" : true} """, expected, equals) shouldBe false
    JsonComparator.compare(""" {"foo" : 23} """, expected, equals) shouldBe false
  }
}
