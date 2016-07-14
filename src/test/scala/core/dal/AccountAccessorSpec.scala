package core.dal

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import common.ErrorMessage
import core.model.{Account, AccountFixture, AccountId}
import squants.market.RUB
import _root_.util.ActorSpecBase

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Promise

class AccountAccessorSpec extends TestKit(ActorSystem("AccountAccessorSpec")) with ActorSpecBase {

  val accountAccessor = system.actorOf(Props(classOf[AccountAccessor]), AccountAccessor.Id)

  val accountPromise: Promise[Account] = Promise()

  def accountIdFtr = accountPromise.future.map(_.id)

  def createAccount(account: Account): AccountId = {
    accountAccessor.ask(AccountAccessor.CreateEntity(account)).mapTo[AccountId].awaitResult
  }

  "AccountAccessor" must {

    val accountAfterWithdrawPromise: Promise[Account] = Promise()

    "Be able to withdraw money from the account" in {
      val account = new AccountFixture().entity

      createAccount(account)
      accountPromise.success(account)

      val withdrawResult = accountAccessor.ask(AccountAccessor.WithdrawMoney(account.id, RUB(50))).
        mapTo[Either[ErrorMessage, Account]].awaitResult

      withdrawResult match {
        case Left(error) => fail()
        case Right(updatedAccount) =>
          account.balance - updatedAccount.balance shouldEqual RUB(50)
          accountAfterWithdrawPromise.success(updatedAccount)
      }
    }

    "Be able to deposit money to the account" in {
      val account = accountAfterWithdrawPromise.future.awaitResult

      val depositResult = accountAccessor.ask(AccountAccessor.DepositMoney(account.id, RUB(50))).
        mapTo[Either[ErrorMessage, Account]].awaitResult

      depositResult match {
        case Left(error) => fail()
        case Right(updatedAccount) =>
          updatedAccount.balance - account.balance shouldEqual RUB(50)
      }
    }

  }
}
