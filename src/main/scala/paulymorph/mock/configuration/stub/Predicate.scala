package paulymorph.mock.configuration.stub

import io.circe.Json

sealed trait Predicate

sealed trait RequestExpectation

case class BodyExpectation(body: Json) extends RequestExpectation
case class PathExpectation(path: String) extends RequestExpectation
case class MethodExpectation(method: String) extends RequestExpectation
case class QueryExpectation(query: Map[String, String]) extends RequestExpectation

sealed trait LeafPredicate extends Predicate {
  def requestExpectation: RequestExpectation
}

case class Equals(requestExpectation: RequestExpectation) extends LeafPredicate
case class StartsWith(requestExpectation: RequestExpectation) extends LeafPredicate
case class Contains(requestExpectation: RequestExpectation) extends LeafPredicate

sealed trait CompoundPredicate extends Predicate {
  def predicates: Seq[Predicate]
}

case class Or(predicates: Seq[Predicate]) extends CompoundPredicate
case class And(predicates: Seq[Predicate]) extends CompoundPredicate
