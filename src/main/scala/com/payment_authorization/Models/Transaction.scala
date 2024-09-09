package com.payment_authorization.Models
import java.util.UUID
import cats.effect.Sync
import cats.syntax.all.*
import fs2.Stream
import skunk.{Codec, Command, Query, Session, Void}
import skunk.codec.all.*
import skunk.syntax.all.*
import com.payment_authorization._
import cats.instances.double

val transaction_codec: Codec[TransactionModel] =
  (uuid, varchar, float8, varchar, varchar).tupled.imap {
    case (id, account, total_amount, mcc, merchant) => TransactionModel(id, account, total_amount, mcc, merchant)
  } { transaction => (transaction.id, transaction.account, transaction.total_amount, transaction.mcc, transaction.merchant) }


case class Transaction(
  account: String, 
  totalAmount: Double,
  mcc: String,
  merchant: String
)

case class TransactionModel(
  id: UUID,
  account: String, 
  total_amount: Double,
  mcc: String,
  merchant: String
)

final class TransactionRepository[F[_]: Sync](session: Session[F]) 
  extends Repository[F, TransactionModel](session) {
  import TransactionRepository.*

    def create(account: String, total_amount: Double, mcc: String, merchant: String): F[UUID] =
    for {
      cmd    <- session.prepare(insert)
      id = UUID.randomUUID()
      _      <- cmd.execute(TransactionModel(id, account, total_amount, mcc, merchant))
    } yield id
  
}

object TransactionRepository {
  def make[F[_]: Sync](session: Session[F]): F[TransactionRepository[F]] =
    Sync[F].delay(new TransactionRepository[F](session))
  
  private val insert: Command[TransactionModel] =
    sql"""
        INSERT INTO users
        VALUES ($transaction_codec)
      """.command
}

def identify_category_in_transaction(transaction: Transaction): String = 
  val category_by_name = transaction.merchant.toUpperCase match {
    case name if name.contains("IFOOD") || name.contains("EATS") || name.contains("PADARIA") => "MEAL"
    case name if name.contains("MERCADO") => "FOOD"
    case _ => "CASH"
  }

  val category_by_mcc = transaction.mcc match {
    case "5411" | "5412" => "FOOD"
    case "5811" | "5812" => "MEAL"
    case _               => "CASH"
  }
  
  if(category_by_name != category_by_mcc) then
    category_by_name
  else 
    category_by_mcc