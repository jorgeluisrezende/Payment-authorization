import com.payment_authorization.Models._
import com.payment_authorization.{Config, DbConnection}
import cats.effect._
import cats.implicits._
import org.http4s.circe._
import org.http4s._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.server.blaze.BlazeServerBuilder
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import skunk.codec.all._
import skunk.syntax.all._
import org.http4s.dsl.Http4sDsl
import natchez.Trace.Implicits.noop
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import cats.effect.unsafe.implicits.global

case class ApiResponse(code: String)
case class ErrorResponse(message: String)
case class InvalidTransactionException(message: String) extends Exception(message)

object Server extends IOApp {
  def routes[F[_]: Concurrent]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F] 
    import dsl._
    implicit val transactionDecoder: EntityDecoder[F, Transaction] = jsonOf[F, Transaction]
    implicit val apiResponseDecoder: EntityDecoder[F, ApiResponse] = jsonOf[F, ApiResponse]

    HttpRoutes.of[F] { 
      case req @ POST -> Root / "authorization" =>{
        req.decode[Transaction] { transaction =>
          TransactionValidator.validator.validate(transaction) match {
            case None =>
              val transaction_category = identify_category_in_transaction(transaction)
              val config: Either[ConfigReaderFailures, Config] = ConfigSource.default.at("db").load[Config]

              config match {
                case Left(_) =>
                  Ok("Configuration loading failed.")
                case Right(configValues) => 
                  var result = DbConnection.single[IO](configValues).use { session =>
                    for {
                      accountRepo <- AccountRepository.make[IO](session)
                      accountBalanceRepository <- AccountBalanceRepository.make[IO](session)
                      
                      account <- accountRepo.findByAccountNumber(transaction.account).flatMap {
                        case Some(acc) =>
                          if (!verify_account_balance(acc, transaction)) 
                            IO.raiseError(new InvalidTransactionException("Account does not have sufficient balance."))
                          else IO.pure(acc)
                        case None =>
                          IO.raiseError(new IllegalArgumentException(s"Account with number ${transaction.account} was not found."))
                      }

                      account_balance <- accountBalanceRepository.findByAccountIdAndCategory(account.id, transaction_category).flatMap {
                        case Some(acc_balance) =>
                          if (!verify_account_category_balance(acc_balance, transaction)) 
                            IO.raiseError(new InvalidTransactionException("Account does not have sufficient balance."))
                          else 
                            IO.pure(acc_balance)
                        case None =>
                          IO.raiseError(new IllegalArgumentException(s"Invalid balance category"))
                      }

                      new_account_balance <- IO(subtract_transaction_from_balance(account_balance, transaction))
                      _ <- IO(accountBalanceRepository.update(new_account_balance)).unsafeRunSync()

                      new_account <- IO.pure(subtract_transaction_from_total_balance(account, transaction))
                      _ <- IO.pure(accountRepo.update(new_account)).unsafeRunSync()

                      res <- IO.pure(new ApiResponse(
                        code="00"
                      ))
                    } yield res
                  }.unsafeRunSync()
                  Ok(result)
              }
            case Some(errors) => BadRequest(errors.asJson)
          }
        }
      }.handleErrorWith (error =>
        val response = new ErrorResponse(message = error.getMessage())
        error match {
          case error: InvalidTransactionException => Ok(new ApiResponse(code="51"))
          case _ => Ok(new ApiResponse(code="07"))
        }
      )
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val apis = Router("/api" -> Server.routes[IO]).orNotFound

    BlazeServerBuilder[IO](runtime.compute)
      .bindHttp(8080, "localhost")
      .withHttpApp(apis)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)

  }
}
