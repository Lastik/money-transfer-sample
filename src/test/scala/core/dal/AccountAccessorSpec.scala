package core.dal

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestKit
import akka.util.Timeout
import util.ActorSpecBase
import akka.pattern.ask
import core.model.{Account, AccountFixture, AccountId}

import scala.concurrent.Promise

class AccountAccessorSpec extends TestKit(ActorSystem("AccountAccessorSpec")) with ActorSpecBase {

  val accountAccessor = system.actorOf(Props(classOf[AccountAccessor]), AccountAccessor.Id)

  val createdAccountIdPromise: Promise[AccountId] = Promise()

  "AccountAccessor" must {

    "Be able to create new account" in {
      val createAccountResult = accountAccessor.ask(AccountAccessor.CreateEntity(new AccountFixture().entity)).awaitResult

      createAccountResult match{
        case accountId: AccountId =>
          createdAccountIdPromise.success(accountId)
        case _ => fail()
      }
    }

    "Be able to find existing account by id" in {
      val accountId = createdAccountIdPromise.future.awaitResult
      val findAccountResult = accountAccessor.ask(AccountAccessor.FindEntityById(accountId)).awaitResult

      findAccountResult match{
        case Some(account: Account) =>
          account.id shouldEqual accountId
        case _  => fail()
      }
    }
  }
}
