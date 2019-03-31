package paulymorph.mock.configuration

import akka.http.scaladsl.server.{Directive0, Directive1, Directives}

sealed trait Predicate extends Directable

object AllPredicate extends Predicate {
  override def toDirective: Directive0 = Directives.pass
}

case class Equals[T](directive: Directive1[T], expected: T) extends Predicate {
  override def toDirective: Directive0 = directive.filter(_.equals(expected)).map(_ => ())
}