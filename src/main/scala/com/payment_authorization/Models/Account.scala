package com.payment_authorization.Models
import java.util.UUID
import cats.effect.Sync
import cats.syntax.all.*
import fs2.Stream
import skunk.{Codec, Command, Query, Session, Void}
import skunk.codec.all.*
import skunk.syntax.all.*
import com.payment_authorization._

case class Account(
  id: UUID, 
  account: String,
  total_balance: Double,
);

val account_codec: Codec[Account] =
  (uuid, varchar, float8).tupled.imap {
    case (id, account, total_balance) => Account(id, account, total_balance)
  } { account => (account.id, account.account, account.total_balance) }

final class AccountRepository[F[_]: Sync](session: Session[F]) 
  extends Repository[F, Account](session) {
  import AccountRepository.*

  def findByAccountNumber(account_number: String): F[Option[Account]] =
    findOneBy(selectByAccountNumber, account_number)
  
  def update(account: Account): F[Unit] =
    update(_update, account)
}

object AccountRepository {
  def make[F[_]: Sync](session: Session[F]): F[AccountRepository[F]] =
    Sync[F].delay(new AccountRepository[F](session))
  
  private val selectByAccountNumber: Query[String, Account] =
    sql"""
        SELECT * FROM account
        WHERE account = $varchar
      """.query(account_codec)
  
  private val _update: Command[Account] =
    sql"""
        UPDATE account
        SET total_balance = $float8
        WHERE id = $uuid
      """.command.contramap { 
        account => (account.total_balance, account.id)
      }
}

def verify_account_balance(account: Account, transaction: Transaction) = 
  account.total_balance >= transaction.totalAmount

def subtract_transaction_from_total_balance(account: Account, transaction: Transaction) = 
  val balance = account.total_balance - transaction.totalAmount
  account.copy(total_balance = balance)
