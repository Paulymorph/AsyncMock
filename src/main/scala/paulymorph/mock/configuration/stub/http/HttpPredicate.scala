package paulymorph.mock.configuration.stub.http

import io.circe.Json

sealed trait HttpPredicate

sealed trait RequestExpectation

case class BodyExpectation(body: Json) extends RequestExpectation
case class PathExpectation(path: String) extends RequestExpectation
case class MethodExpectation(method: String) extends RequestExpectation
case class QueryExpectation(query: Map[String, String]) extends RequestExpectation

sealed trait LeafHttpPredicate extends HttpPredicate {
  def requestExpectation: RequestExpectation
}

case class Equals(requestExpectation: RequestExpectation) extends LeafHttpPredicate
case class StartsWith(requestExpectation: RequestExpectation) extends LeafHttpPredicate
case class Contains(requestExpectation: RequestExpectation) extends LeafHttpPredicate

sealed trait CompoundHttpPredicate extends HttpPredicate {
  def predicates: Seq[HttpPredicate]
}

case class Or(predicates: Seq[HttpPredicate]) extends CompoundHttpPredicate
case class And(predicates: Seq[HttpPredicate]) extends CompoundHttpPredicate
