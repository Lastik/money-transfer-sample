package core.services

import akka.actor.Actor
import akka.pattern.{ask, pipe}
import common.actors.LookupBusinessActor
import common.{ErrorMessage, ServiceSuccess}
import core.DefaultTimeout
import core.dal.{AccountAccessor, CustomerAccessor}
import core.model.{Account, AccountId, CustomerId}
import core.services.AccountService.Errors.CustomerWithSpecifiedIdDoesntExistErrorMsg
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
          for {
            createdAccountId <- accountAccessor.ask(AccountAccessor.CreateEntity(account)).mapTo[AccountId]
            _ <- customerAccessor.ask(CustomerAccessor.LinkCustomerWithAccount(account.customerId, account.id))
          } yield Right(ServiceSuccess(createdAccountId))
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

  def findCustomerAccounts(customerId: CustomerId): Future[Either[ErrorMessage, ServiceSuccess[AccountsDTO]]] = {
    checkIfCustomerExists(customerId).flatMap(
      customerExists =>
        if (customerExists)
          for {
            accountsIds <- customerAccessor.ask(CustomerAccessor.GetCustomerAccounts(customerId)).mapTo[List[AccountId]]
            accounts <- Future.sequence(accountsIds.map(accountId => accountAccessor.ask(AccountAccessor.FindEntityById(accountId)).
              mapTo[Option[Account]])).map(_.flatten)
          } yield {
            Right(ServiceSuccess(AccountsDTO(accounts)))
          }
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
