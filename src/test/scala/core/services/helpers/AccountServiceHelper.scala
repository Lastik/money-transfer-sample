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

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.{Future, Promise}

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

  def createAccounts(customerId: CustomerId, amountOfAccounts: Int, balanceOnEachAccount: Money = RUB(1000)) = {
    Future.sequence((1 to amountOfAccounts).map(_ =>{
      accountService.ask(AccountService.CreateAccount(AccountDTO(customerId = customerId, balance = balanceOnEachAccount))).
        mapTo[Either[ErrorMessage, ServiceSuccess[AccountId]]].map{
        case Right(ServiceSuccess(accountId)) =>
          accountId
        case _ =>
          throw new IllegalStateException()
      }
    })).map(_.toList).awaitResult
  }

  implicit class CreateAccountsResultExtensions(createAccountsResult: List[AccountId]) {
    def saveToPromise(promise: Promise[List[AccountId]]): List[AccountId] = {
      promise.success(createAccountsResult)
      createAccountsResult
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
