package com.payment_authorization

import cats.effect.{Temporal, Resource}
import cats.effect.std.Console
import cats.effect.Sync
import cats.syntax.all.*
import fs2.io.net.Network
import natchez.Trace
import skunk.{Session, Command, Query}
import com.payment_authorization.{Config}

object DbConnection {
  def single[F[_] : Temporal : Trace : Network : Console](config: Config): Resource[F, Session[F]] =
    Session.single(
      host = config.host,
      port = config.port,
      user = config.username,
      password = Some(config.password),
      database = config.database,
    )

  def pooled[F[_] : Temporal : Trace : Network : Console](config: Config): Resource[F, Resource[F, Session[F]]] =
    Session.pooled(
      host = config.host,
      port = config.port,
      user = config.username,
      password = Some(config.password),
      database = config.database,
      max = 10
    )
}

trait Repository[F[_], E](session: Session[F]) {
  protected def findOneBy[A](query: Query[A, E], argument: A)(using F: Sync[F]): F[Option[E]] =
    for {
      preparedQuery <- session.prepare(query)
      result        <- preparedQuery.option(argument)
    } yield result

  protected def update[A](command: Command[A], argument: A)(using F: Sync[F]): F[Unit] = 
    for {
      preparedCommand <- session.prepare(command)
      _ <- F.delay(println(s"Prepared command: $preparedCommand with argument: $argument"))
      affectedRows <- preparedCommand.execute(argument)
      _ <- F.delay(println(s"Affected rows: $affectedRows"))
    } yield ()
}