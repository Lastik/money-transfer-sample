package core.services

import akka.actor.Actor
import akka.pattern.{ask, pipe}
import common.actors.LookupBusinessActor
import common.{ErrorMessage, ServiceSuccess}
import core.DefaultTimeout
import core.dal.{AccountAccessor, CustomerAccessor}
import core.model.{Account, AccountId, CustomerId}
import squants.market.Money

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AccountService {

  val Id = "account-service"

  case class CreateAccount(accountDTO: AccountDTO)

  case class FindAccountById(accountId: AccountId)

  case class FindCustomerAccounts(customerId: CustomerId)

  object Errors {

    case object CustomerWithSpecifiedIdDoesntExistErrorMsg extends ErrorMessage {
      val text = "Customer with specified id doesn't exist"
    }

    case object AccountWithSpecifiedIdNotFoundErrorMsg extends ErrorMessage {
      val text = "Account with specified id not found"
    }

  }

}

class AccountService extends Actor with LookupBusinessActor with DefaultTimeout {

  import AccountService._

  val customerAccessor = lookupByContext(CustomerAccessor.Id)
  val accountAccessor = lookupByContext(AccountAccessor.Id)

  def receive: Receive = {
    case CreateAccount(accountDTO) =>
      createAccount(Account.apply(accountDTO)) pipeTo sender
    case FindAccountById(accountId) =>
      findAccountById(accountId) pipeTo sender
    case FindCustomerAccounts(customerId) =>
      findCustomerAccounts(customerId) pipeTo sender
  }

  def createAccount(account: Account) = {
    checkIfCustomerExists(account.customerId).flatMap(
      customerExists =>
        if (customerExists)
          accountAccessor.ask(AccountAccessor.CreateEntity(account)).mapTo[AccountId].
            map(accountId => Right(ServiceSuccess(accountId)))
        else
          Future successful Left(Errors.CustomerWithSpecifiedIdDoesntExistErrorMsg)
    )
  }

  def findAccountById(accountId: AccountId) = {
    accountAccessor.ask(AccountAccessor.FindEntityById(accountId)).mapTo[Option[Account]].map{
      case Some(account) => Right(ServiceSuccess(account))
      case None => Left(Errors.AccountWithSpecifiedIdNotFoundErrorMsg)
    }
  }

  def findCustomerAccounts(customerId: CustomerId) = {
    checkIfCustomerExists(customerId).flatMap(
      customerExists =>
        if (customerExists)
          accountAccessor.ask(AccountAccessor.FindCustomerAccounts(customerId)).mapTo[List[Account]]
            map(accounts => Right(ServiceSuccess(AccountsDTO(accounts))))
        else
          Future successful Left(Errors.CustomerWithSpecifiedIdDoesntExistErrorMsg)
    )
  }

  def checkIfCustomerExists(customerId: CustomerId) = {
    customerAccessor.ask(CustomerAccessor.CheckIfEntityExistsById(customerId)).mapTo[Boolean]
  }
}

case class AccountDTO(customerId: CustomerId, balance: Money)

case class AccountsDTO(accounts: List[Account])
