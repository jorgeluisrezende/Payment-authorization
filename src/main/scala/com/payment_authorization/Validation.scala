import cats.data.NonEmptyList
import cats.implicits._
import com.payment_authorization.Models._
import com.payment_authorization.Models.{Transaction}

case class FieldError(
    fieldName: String,
    message: String
)

type ValidationResult = Option[NonEmptyList[FieldError]]

trait Validator[T] {
    def validate(target: T): ValidationResult
}
  
trait FieldValidator[T] {
    def validate(field: T, fieldName: String): ValidationResult
}

case object NotEmpty extends FieldValidator[String] {
    def validate(target: String, fieldName: String) =
      if (target.isEmpty) NonEmptyList.of(FieldError(fieldName, "Must not be empty")).some else None
}

case class WithLength(min: Int, max: Int) extends FieldValidator[String] {
    def validate(target: String, fieldName: String) =
        if (target.length < min || target.length > max)
        NonEmptyList.of(FieldError(fieldName, s"Length must be between $min and $max")).some else None
}

case object TransactionValidator {
    implicit val validator: Validator[Transaction] = (target: Transaction) => {
      NotEmpty.validate(target.account, "account") |+|
      NotEmpty.validate(target.mcc, "mcc") |+|
      NotEmpty.validate(target.merchant, "merchant")
    }
}