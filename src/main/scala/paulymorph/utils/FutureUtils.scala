package paulymorph.utils

import scala.concurrent.{ExecutionContext, Future}

trait FutureUtils {
  def optTraverse[T, U](option: Option[T])(f: T => Future[U])
                       (implicit ec: ExecutionContext): Future[Option[U]] = {
    option match {
      case Some(value) => f(value)
        .map(Some.apply)
      case None => Future.successful(None)
    }
  }

  def eitherTraverse[L, R, U](either: Either[L, R])(f: R => Future[U])
                             (implicit ec: ExecutionContext): Future[Either[L, U]] = {
    either match {
      case Left(value) => Future.successful(Left(value))
      case Right(value) => f(value)
        .map(result => Right(result))
    }
  }
}

object FutureUtils extends FutureUtils {
}
