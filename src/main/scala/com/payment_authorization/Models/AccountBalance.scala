package com.payment_authorization.Models;
import java.util.UUID
import cats.effect.Sync
import cats.syntax.all.*
import fs2.Stream
import skunk.{Codec, Command, Query, Session, Void}
import skunk.codec.all.*
import skunk.syntax.all.*
import com.payment_authorization._

case class AccountBalance(
  id: UUID,
  account_id: UUID,
  category: String,
  balance: Double
);

val account_balance_codec: Codec[AccountBalance] =
  (uuid, uuid, varchar, float8).tupled.imap {
    case (id, account_id, category, balance) => AccountBalance(id, account_id, category, balance)
  } { account_balance => (account_balance.id, account_balance.account_id, account_balance.category, account_balance.balance) }

final class AccountBalanceRepository[F[_]: Sync](session: Session[F]) 
  extends Repository[F, AccountBalance](session) {
  import AccountBalanceRepository.*

  def findByAccountIdAndCategory(account_id: UUID, category: String): F[Option[AccountBalance]] =
      findOneBy(selectByAccountIdAndCategory, (account_id, category))
  
  def update(account_balance: AccountBalance): F[Unit] =
    update(_update, account_balance)
}

object AccountBalanceRepository {
  def make[F[_]: Sync](session: Session[F]): F[AccountBalanceRepository[F]] =
    Sync[F].delay(new AccountBalanceRepository[F](session))
  
  private val selectByAccountIdAndCategory: Query[(UUID, String), AccountBalance] =
    sql"""
        SELECT * FROM account_balance
        WHERE account_id = $uuid and
        category = $varchar
      """.query(account_balance_codec)

  private val _update: Command[AccountBalance] =
    sql"""
        UPDATE account_balance
        SET balance = $float8
        WHERE id = $uuid
      """.command.contramap { account_balance => (account_balance.balance, account_balance.id)
    }
}

def verify_account_category_balance(account_balance: AccountBalance, transaction: Transaction): Boolean = 
  account_balance.balance >= transaction.totalAmount
  
def subtract_transaction_from_balance(account_balance: AccountBalance, transaction: Transaction): AccountBalance = 
  val balance = account_balance.balance - transaction.totalAmount
  account_balance.copy(balance = balance)
