package core.services

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestKit
import core.dal.{AccountAccessor, CustomerAccessor}
import core.model.{Account, AccountId, CustomerId}
import util.ActorSpecBase
import akka.pattern.ask
import common.{ErrorMessage, ServiceSuccess}
import squants.Money
import squants.market.RUB

import scala.concurrent.Promise

class AccountServiceSpec extends TestKit(ActorSystem("AccountService")) with ActorSpecBase {

  val accountAccessor = system.actorOf(Props(classOf[AccountAccessor]), AccountAccessor.Id)
  val customerAccessor = system.actorOf(Props(classOf[CustomerAccessor]), CustomerAccessor.Id)

  val accountService = system.actorOf(Props(classOf[AccountService]), AccountService.Id)
  val customerService = system.actorOf(Props(classOf[CustomerService]), CustomerService.Id)

  val customerIdPromise: Promise[CustomerId] = Promise()

  val accountIdPromise: Promise[AccountId] = Promise()

  def getCreatedCustomerId = customerIdPromise.future.awaitResult

  def getCreatedAccountId = accountIdPromise.future.awaitResult

  override protected def beforeAll(): Unit = {
    customerIdPromise.success(createCustomer("Some Customer"))
  }

  "AccountService" must {

    "Be able to create account for existing customer" in {

      val customerId = customerIdPromise.future.awaitResult

      val createAccountResult = createAccount(customerId, RUB(1000))

      createAccountResult match {
        case Left(errorMessage) => fail(errorMessage.text)
        case Right(ServiceSuccess(accountId)) =>
          accountIdPromise.success(accountId)
      }
    }

    "Not be able to create account for non existing customer" in {

      val customerId = CustomerId()

      val createAccountResult = createAccount(customerId, RUB(1000))

      createAccountResult match {
        case Left(errorMessage) =>
        case Right(ServiceSuccess(accountId)) => fail()
      }
    }

    "Be able to find existing account by it's id" in {

      val accountToFindId = getCreatedAccountId

      val findAccountResult = findAccountById(accountToFindId)

      findAccountResult match {
        case Left(errorMessage) => fail(errorMessage.text)
        case Right(ServiceSuccess(foundAccount)) =>
          foundAccount.id shouldEqual accountToFindId
      }
    }

    "Not be able to find non existing account by it's id" in {

      val accountToFindId = AccountId()

      val findAccountResult = findAccountById(accountToFindId)

      findAccountResult match {
        case Left(errorMessage) =>
        case Right(ServiceSuccess(foundAccount)) => fail()
      }
    }

    "Be able to find all the accounts for existing customer" in {

      val customerId = getCreatedCustomerId

      val findCustomerAccountsResult = findCustomerAccounts(customerId)

      findCustomerAccountsResult match {
        case Left(errorMessage) => fail()
        case Right(ServiceSuccess(foundAccountsDTO)) => foundAccountsDTO.accounts.length shouldEqual 1
      }
    }

    "Not able to find all the accounts for non existing customer" in {

      val customerId = CustomerId()

      val findCustomerAccountsResult = findCustomerAccounts(customerId)

      findCustomerAccountsResult match {
        case Left(errorMessage) =>
        case Right(ServiceSuccess(foundAccountsDTO)) => fail()
      }
    }
  }

  def createCustomer(name: String) = {
    customerService.ask(CustomerService.CreateCustomer(CustomerDTO(name))).
      mapTo[CustomerId].awaitResult
  }

  def createAccount(customerId: CustomerId, balance: Money) = {
    accountService.ask(AccountService.CreateAccount(AccountDTO(customerId = customerId, balance = balance))).
      mapTo[Either[ErrorMessage, ServiceSuccess[AccountId]]].awaitResult
  }

  def findAccountById(accountId: AccountId) = {
    accountService.ask(AccountService.FindAccountById(accountId)).
      mapTo[Either[ErrorMessage, ServiceSuccess[Account]]].awaitResult
  }

  def findCustomerAccounts(customerId: CustomerId) = {
    accountService.ask(AccountService.FindCustomerAccounts(customerId)).
      mapTo[Either[ErrorMessage, ServiceSuccess[AccountsDTO]]].awaitResult
  }
}
