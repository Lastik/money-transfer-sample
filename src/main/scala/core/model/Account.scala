package core.model

import java.util.UUID

import common.ErrorMessage
import core.services.AccountDTO
import squants.market._

case class AccountId(id: String =  UUID.randomUUID().toString) extends ModelEntityKey

object AccountId{
  implicit def fromUUID(uuid: UUID): AccountId = {
    AccountId(id = uuid.toString)
  }
}

case class Account(id: AccountId = AccountId(), customerId: CustomerId, balance: Money)(implicit moneyContext: MoneyContext) extends ModelEntity {

  type KeyType = AccountId

  import Account.Errors._

  require(balance.value >= 0, "Balance on the account should be >= 0")

  def withdrawMoney(amount: Money) = {
    updateMoneyBalance(-amount)
  }

  def depositMoney(amount: Money) = {
    updateMoneyBalance(amount)
  }

  private def updateMoneyBalance(delta: Money) = {
    val updatedAmountOfMoney = balance + delta

    if (updatedAmountOfMoney.value < 0) {
      Left(InsufficientAmountOfMoneyOnAccountErrorMgs)
    }
    else {
      Right(this.copy(balance = balance + delta))
    }
  }
}

object Account {

  def apply(accountDTO: AccountDTO): Account = {
    Account(customerId = accountDTO.customerId, balance = accountDTO.balance)
  }

  implicit val moneyContext = MoneyContext(
    defaultCurrency = RUB,
    currencies = Set(RUB, USD),
    rates = Seq(
      CurrencyExchangeRate(
        base = USD(1),
        counter = RUB(60)
      ),
      CurrencyExchangeRate(
        base = RUB(60),
        counter = USD(1)
      )
    )
  )

  object Errors {

    case object InsufficientAmountOfMoneyOnAccountErrorMgs extends ErrorMessage {
      val text = "Insufficient amount of money on the account!"
    }

    case object WrongCurrencyForAmountSpecifiedErrorMsg extends ErrorMessage {
      val text = "Wrong currency for amount specified error msg"
    }

  }

}
