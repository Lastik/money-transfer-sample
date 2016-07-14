package core.services

import akka.actor.Actor
import akka.pattern.{ask, pipe}
import common.actors.LookupBusinessActor
import core.DefaultTimeout
import core.dal.AccountAccessor
import core.model.{Account, AccountId, CustomerId}
import core.services.AccountService.{CreateAccount, GetAccountById, GetCustomerAccounts}
import squants.market.Money

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AccountService {

  val Id = "account-service"

  case class CreateAccount(accountDTO: AccountDTO)

  case class GetAccountById(accountId: AccountId)

  case class GetCustomerAccounts(customerId: CustomerId)

}

class AccountService extends Actor with LookupBusinessActor with DefaultTimeout {

  val accountAccessor = lookupByContext(AccountAccessor.Id)

  def receive: Receive = {

    case CreateAccount(accountDTO) =>
      accountAccessor.ask(AccountAccessor.CreateEntity(Account.apply(accountDTO))) pipeTo sender
    case GetAccountById(accountId) =>
      accountAccessor.ask(AccountAccessor.GetEntityById(accountId)) pipeTo sender
    case GetCustomerAccounts(customerId) =>
      accountAccessor.ask(AccountAccessor.GetCustomerAccounts(customerId)).mapTo[List[Account]].map(AccountsDTO) pipeTo sender
  }

  def findAccountById(accountId: AccountId): Future[Option[Account]] = {
    accountAccessor.ask(AccountAccessor.FindEntityById(accountId)).mapTo[Option[Account]]
  }
}

case class AccountDTO(customerId: CustomerId, balance: Money)

case class AccountsDTO(accounts: List[Account])
