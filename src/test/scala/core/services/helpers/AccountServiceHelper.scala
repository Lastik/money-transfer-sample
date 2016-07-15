package core.services.helpers

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import common.{ErrorMessage, ServiceSuccess}
import core.model.{Account, AccountId, CustomerId}
import core.services._
import squants._
import squants.market.RUB
import util.AwaitHelper

import scala.concurrent.Promise

trait AccountServiceHelper {
  this: AwaitHelper =>

  implicit def timeout: Timeout

  def accountService: ActorRef

  def createAccount(customerId: CustomerId, balance: Money = RUB(1000)) = {
    accountService.ask(AccountService.CreateAccount(AccountDTO(customerId = customerId, balance = balance))).
      mapTo[Either[ErrorMessage, ServiceSuccess[AccountId]]].awaitResult
  }

  implicit class CreateAccountResultExtensions(createAccountResult: Either[ErrorMessage, ServiceSuccess[AccountId]]) {
    def saveToPromise(promise: Promise[AccountId]): Either[ErrorMessage, ServiceSuccess[AccountId]] = {
      createAccountResult match {
        case Right(ServiceSuccess(accountId)) =>
          promise.success(accountId)
        case _ =>
          throw new IllegalStateException()
      }
      createAccountResult
    }
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
