package core.model

import java.util.UUID

import squants.market._

case class AccountId(id: String =  UUID.randomUUID().toString) extends ModelEntityKey

object AccountId{
  implicit def fromUUID(uuid: UUID) = {
    AccountId(id = uuid.toString)
  }
}

case class Account(id: AccountId = AccountId(), customerId: CustomerId, money: Money)(implicit val moneyContext: MoneyContext) extends ModelEntity {


  def withdrawMoney(toWithdraw: Money) = this.copy(money = money - toWithdraw)

  def depositMoney(toDeposit: Money) = this.copy(money = money + toDeposit)
}

object Account {

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

  val Values = List(
    Account(customerId = Customer.IvanIvanov.id, money = USD(10000)),
    Account(customerId = Customer.IvanIvanov.id, money = RUB(5000)),
    Account(customerId = Customer.VasyaPupkin.id, money = RUB(500000))
  )
}
