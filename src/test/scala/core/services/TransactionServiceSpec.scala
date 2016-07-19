package core.services

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestKit
import core.dal.{AccountAccessor, CustomerAccessor}
import core.model.{AccountId, CustomerId, Transaction}
import util.ActorSpecBase
import akka.pattern.ask
import common.{ErrorMessage, ServiceSuccess}
import core.services.helpers.{AccountServiceHelper, CustomerServiceHelper}
import squants.market.{RUB, USD}

import scala.concurrent.Promise

class TransactionServiceSpec extends TestKit(ActorSystem("TransactionService")) with ActorSpecBase
  with CustomerServiceHelper with AccountServiceHelper {

  val customerAccessor = system.actorOf(Props(classOf[CustomerAccessor], 3), CustomerAccessor.Id)
  val accountAccessor = system.actorOf(Props(classOf[AccountAccessor], 3), AccountAccessor.Id)

  val customerService = system.actorOf(Props(classOf[CustomerService]), CustomerService.Id)
  val accountService = system.actorOf(Props(classOf[AccountService]), AccountService.Id)
  val transactionService = system.actorOf(Props(classOf[TransactionService]), TransactionService.Id)

  val customerStanSmithIdPromise: Promise[CustomerId] = Promise()
  val customerHomerSimpsonIdPromise: Promise[CustomerId] = Promise()

  val customerStanSmithDollarAccountIdPromise: Promise[AccountId] = Promise()
  val customerStanSmithRublesAccountIdPromise: Promise[AccountId] = Promise()

  val customerHomerSimpsonDollarAccountIdPromise: Promise[AccountId] = Promise()

  def getCustomerStanSmithDollarAccountId = customerStanSmithDollarAccountIdPromise.future.awaitResult
  def getCustomerStanSmithRublesAccountId = customerStanSmithRublesAccountIdPromise.future.awaitResult
  def getCustomerHomerSimpsonDollarAccountId = customerHomerSimpsonDollarAccountIdPromise.future.awaitResult

  override protected def beforeAll(): Unit = {
    val stanSmithCustomerId = createCustomer("Stan Smith")
    val homerSimpsonCustomerId = createCustomer("Stan Smith")

    customerStanSmithIdPromise.success(stanSmithCustomerId)
    customerHomerSimpsonIdPromise.success(homerSimpsonCustomerId)

    createAccount(stanSmithCustomerId, USD(1000)).saveToPromise(customerStanSmithDollarAccountIdPromise)
    createAccount(stanSmithCustomerId, RUB(20000)).saveToPromise(customerStanSmithRublesAccountIdPromise)

    createAccount(homerSimpsonCustomerId, USD(500)).saveToPromise(customerHomerSimpsonDollarAccountIdPromise)
  }

  "TransactionService" must {

    "Be able to transfer money from one dollar account to another" in {

      val stanSmithDollarAccountId = getCustomerStanSmithDollarAccountId
      val homerSimpsonDollarAccountId = getCustomerHomerSimpsonDollarAccountId

      val transaction = Transaction(
        from = stanSmithDollarAccountId,
        to = homerSimpsonDollarAccountId,
        amount = USD(100)
      )

      val processTransactionResult = processTransaction(transaction)

      processTransactionResult match {
        case Left(errorMessage) => fail(errorMessage.text)
        case _ =>
      }

      val stanSmithDollarAccountAfterTransfer = findAccountById(stanSmithDollarAccountId).right.get.result
      stanSmithDollarAccountAfterTransfer.balance shouldEqual USD(900)

      val homerSimpsonDollarAccountAfterTransfer = findAccountById(homerSimpsonDollarAccountId).right.get.result
      homerSimpsonDollarAccountAfterTransfer.balance shouldEqual USD(600)
    }

    "Fail when there is not enough amount of money on the account" in {

      val stanSmithDollarAccountId = getCustomerStanSmithDollarAccountId
      val homerSimpsonDollarAccountId = getCustomerHomerSimpsonDollarAccountId

      val transaction = Transaction(
        from = stanSmithDollarAccountId,
        to = homerSimpsonDollarAccountId,
        amount = USD(10000)
      )

      val processTransactionResult = processTransaction(transaction)

      processTransactionResult match {
        case Left(errorMessage) =>
        case Right(_) => fail()
      }

      val stanSmithDollarAccountAfterTransfer = findAccountById(stanSmithDollarAccountId).right.get.result
      stanSmithDollarAccountAfterTransfer.balance shouldEqual USD(900)

      val homerSimpsonDollarAccountAfterTransfer = findAccountById(homerSimpsonDollarAccountId).right.get.result
      homerSimpsonDollarAccountAfterTransfer.balance shouldEqual USD(600)
    }

    val mrXAccountId = AccountId()

    "Fail when source account doesn't exist" in {

      val homerSimpsonDollarAccountId = getCustomerHomerSimpsonDollarAccountId

      val transaction = Transaction(
        from = mrXAccountId,
        to = homerSimpsonDollarAccountId,
        amount = USD(10000)
      )

      val processTransactionResult = processTransaction(transaction)

      processTransactionResult match {
        case Left(errorMessage) =>
        case Right(_) => fail()
      }

      val homerSimpsonDollarAccountAfterTransfer = findAccountById(homerSimpsonDollarAccountId).right.get.result
      homerSimpsonDollarAccountAfterTransfer.balance shouldEqual USD(600)
    }

    "Fail when target account doesn't exist" in {

      val homerSimpsonDollarAccountId = getCustomerHomerSimpsonDollarAccountId

      val transaction = Transaction(
        from = homerSimpsonDollarAccountId,
        to = mrXAccountId,
        amount = USD(10000)
      )

      val processTransactionResult = processTransaction(transaction)

      processTransactionResult match {
        case Left(errorMessage) =>
        case Right(_) => fail()
      }

      val homerSimpsonDollarAccountAfterTransfer = findAccountById(homerSimpsonDollarAccountId).right.get.result
      homerSimpsonDollarAccountAfterTransfer.balance shouldEqual USD(600)
    }

    "Be able to transfer money from rubles account to dollar account (make conversion)" in {
      val stanSmithRublesAccountId = getCustomerStanSmithRublesAccountId
      val homerSimpsonDollarAccountId = getCustomerHomerSimpsonDollarAccountId

      val transaction = Transaction(
        from = stanSmithRublesAccountId,
        to = homerSimpsonDollarAccountId,
        amount = RUB(60)
      )

      val processTransactionResult = processTransaction(transaction)

      processTransactionResult match {
        case Left(errorMessage) => fail(errorMessage.text)
        case Right(_) =>
      }

      val stanSmithRublesAccountAfterTransfer = findAccountById(stanSmithRublesAccountId).right.get.result
      stanSmithRublesAccountAfterTransfer.balance shouldEqual RUB(20000 - 60)

      val homerSimpsonDollarAccountAfterTransfer = findAccountById(homerSimpsonDollarAccountId).right.get.result
      homerSimpsonDollarAccountAfterTransfer.balance > USD(600) shouldEqual true
    }
  }


  def processTransaction(transaction: Transaction): Either[ErrorMessage, ServiceSuccess[String]] = {
    transactionService.ask(TransactionService.ProcessTransaction(transaction)).
      mapTo[Either[ErrorMessage, ServiceSuccess[String]]].awaitResult
  }
}