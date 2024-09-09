import com.payment_authorization.Models._
import java.util.UUID
// For more information on writing tests, see
// https://scalameta.org/munit/docs/getting-started.html
class Tests extends munit.FunSuite {
  test("Should validate if account has balance with result true") {
    val account_mock = new Account(UUID(10, 10), "123", 300.00)
    val transaction_mock = new Transaction(account = "123", 50, "5411", "PAG*JoseDaSilva")

    val result = verify_account_balance(account_mock, transaction_mock)
    assertEquals(result, true)
  }

  test("Should validate if account has balance with result false") {
    val account_mock = new Account(UUID(10, 10), "123", 300.00)
    val transaction_mock = new Transaction(account = "123", 500, "5411", "PAG*JoseDaSilva")

    val result = verify_account_balance(account_mock, transaction_mock)
    assertEquals(result, false)
  }

  test("Should test if subtract returns the right value in account") {
    val account_mock = new Account(UUID(10, 10), "123", 300.00)
    val transaction_mock = new Transaction(account = "123", 50, "5411", "PAG*JoseDaSilva")

    val result = subtract_transaction_from_total_balance(account_mock, transaction_mock)
    assertEquals(result.total_balance, 250.00)
  }

  test("Should validate if account_balance has balance in category with result true") {
    val account_balance_mock = new AccountBalance(UUID(10, 10), account_id = UUID(10, 10), balance = 300.00, category = "FOOD")
    val transaction_mock = new Transaction(account = "123", 50, "5411", "PAG*JoseDaSilva")

    val result = verify_account_category_balance(account_balance_mock, transaction_mock)
    assertEquals(result, true)
  }

  test("Should validate if account_balance has balance in category with result false") {
    val account_balance_mock = new AccountBalance(UUID(10, 10), account_id = UUID(10, 10), balance = 300.00, category = "FOOD")
    val transaction_mock = new Transaction(account = "123", 500, "5411", "PAG*JoseDaSilva")

    val result = verify_account_category_balance(account_balance_mock, transaction_mock)
    assertEquals(result, false)
  }

    test("Should test if subtract returns the right value in account") {
    val account_balance_mock = new AccountBalance(UUID(10, 10), account_id = UUID(10, 10), balance = 300.00, category = "FOOD")
    val transaction_mock = new Transaction(account = "123", 50, "5411", "PAG*JoseDaSilva")

    val result = subtract_transaction_from_balance(account_balance_mock, transaction_mock)
    assertEquals(result.balance, 250.00)
  }

}
