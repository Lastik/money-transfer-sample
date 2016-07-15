package core.services.helpers

import akka.actor.ActorRef
import akka.pattern.ask
import common.{ErrorMessage, ServiceSuccess}
import core.DefaultTimeout
import core.model.{Account, AccountId, CustomerId}
import core.services._
import squants._
import util.{ActorSpecBase, AwaitHelper}

import scala.concurrent.Promise

trait AccountServiceHelper {
  this: ActorSpecBase =>

  def accountService: ActorRef

  def createAccount(customerId: CustomerId, balance: Money) = {
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
