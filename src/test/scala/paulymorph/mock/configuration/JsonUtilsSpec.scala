package paulymorph.mock.configuration

import io.circe.Json
import io.circe.parser.decode
import org.scalatest.{FlatSpec, Matchers}
import paulymorph.mock.configuration.stub._

class JsonUtilsSpec extends FlatSpec with Matchers {

  import JsonUtils._
  import org.scalatest.EitherValues._

  "JsonUtils" should "parse equals predicate" in {
    val rawBody = """{"key": "value"}"""
    val rawPath = "/bla/sdf"
    val rawQuery = """{ "q": "q1" }"""
    val rawEquals =
      s"""
         |{
         | "equals": {
         |     "body": $rawBody,
         |     "path": "$rawPath",
         |     "query": $rawQuery,
         |     "method": "post"
         |   }
         |}
      """.stripMargin

    val expectations = BodyExpectation(decode[Json](rawBody).right.value) ::
      PathExpectation(rawPath) ::
      QueryExpectation(Map("q" -> "q1")) ::
      MethodExpectation("post") ::
      Nil

    val decodedPredicate = decode[Predicate](rawEquals).right.value
    decodedPredicate match {
      case And(predicates) => predicates should contain theSameElementsAs expectations.map(Equals)
      case _ => ???
    }
  }
}
