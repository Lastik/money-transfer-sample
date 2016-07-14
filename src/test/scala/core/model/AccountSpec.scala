package core.model

import common.ModelEntityFixture
import core.model.Account._
import org.scalatest.{FunSpec, Matchers}
import squants.market.RUB

class AccountSpec extends FunSpec with Matchers {

  describe("Account") {

    it("Check the balance on the account (should be always >=0) while account creation") {

      an[IllegalArgumentException] should be thrownBy {
        val accountWithNegativeBalance = Account(customerId = CustomerId(), balance = RUB(-1))
      }

      val accountWithPositiveBalance = Account(customerId = CustomerId(), balance = RUB(1))

      accountWithPositiveBalance.balance shouldEqual RUB(1)
    }

    val accountFixture = new AccountFixture()

    it("Be able to withdraw money when there is sufficient amount of them") {

      val account = accountFixture.entity

      account.withdrawMoney(RUB(30)) match {
        case Left(errorMessage) => fail(errorMessage.text)
        case Right(accountAfterMoneyWithdraw) =>
          accountAfterMoneyWithdraw.balance shouldEqual RUB(70)
      }
    }

    it("Refuse to withdraw money when there is insufficient amount of them on the account") {

      val account = accountFixture.entity

      account.withdrawMoney(RUB(200)) match {
        case Left(errorMessage) =>
        case Right(accountAfterMoneyWithdraw) =>
          fail("There was not enougth of money on the account but we were able to somehow withdraw money")
      }
    }
  }
}

class AccountFixture extends ModelEntityFixture[Account]{
  def entity = Account(customerId = CustomerId(), balance = RUB(100))
}