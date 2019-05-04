package paulymorph.utils

import io.circe.Json
import io.circe.parser.decode

object JsonComparator {
  def compare(actual: String, expected: Json, comparator: (String, String) => Boolean): Boolean = {
    expected.asString match {
      case Some(expectedString) => comparator(actual, expectedString)
      case _ => (
        for {
          actualJson <- decode[Json](actual)
          compared = compare(actualJson, expected, comparator)
        } yield compared
        ).getOrElse(false)
    }
  }

  def compare(actual: Json, expected: Json, comparator: (String, String) => Boolean): Boolean = {
    (actual, expected) match {
      case _ if actual.isString && expected.isString =>
        comparator(actual.asString.get, expected.asString.get)
      case _ if actual.isObject && expected.isObject =>
        expected.asObject.get.toVector.foldLeft(true) {
          case (false, _) => false
          case (_, (key, expectedValue)) =>
            (for {
              actualValue <- actual.hcursor.get[Json](key)
              compared = compare(actualValue, expectedValue, comparator)
            } yield compared).getOrElse(false)
        }
      case _ if actual.isArray && expected.isArray => ???
      case _ => actual == expected
    }
  }
}
