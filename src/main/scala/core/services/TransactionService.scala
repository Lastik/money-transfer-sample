package core.services

import akka.actor.Actor
import common.{ErrorMessage, ServiceSuccess}
import common.actors.LookupBusinessActor
import core.DefaultTimeout
import core.dal.{AccountAccessor, CustomerAccessor}
import core.model.{Account, AccountId, Transaction}
import squants.Money
import akka.pattern.{ask, pipe}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TransactionService {

  val Id = "transaction-service"

  case class ProcessTransaction(transaction: Transaction)

  object Error {

    case object TargetAccountDoesntExistErrorMgs extends ErrorMessage {
      val text = "Target account doesn't exist"
    }

    case object SourceAccountDoesntExistErrorMsg extends ErrorMessage {
      val text = "Source account doesn't exist"
    }

  }
}

class TransactionService extends Actor with LookupBusinessActor with DefaultTimeout {

  import TransactionService._

  val customerAccessor = lookupByContext(CustomerAccessor.Id)
  val accountAccessor = lookupByContext(AccountAccessor.Id)

  def receive: Receive = {
    case ProcessTransaction(transaction) =>
      processTransaction(transaction) pipeTo sender
  }

  def processTransaction(transaction: Transaction): Future[Either[ErrorMessage, ServiceSuccess[String]]] = {

    val (fromAccountId, toAccountId, amount) = (transaction.from, transaction.to, transaction.amount)

    (for {
      fromAccountOpt <- findAccountById(fromAccountId)
      toAccountOpt <- findAccountById(toAccountId)
    } yield {
      (fromAccountOpt, toAccountOpt) match {
        case (Some(fromAccount), Some(toAccount)) =>
          withdrawMoney(fromAccountId, amount).flatMap {
            case Left(errorMessage) => Future successful Left(errorMessage)
            case Right(_) =>
              depositMoney(toAccountId, amount).flatMap {
                case Left(errorMessage) =>

                  def rollbackTransaction() = depositMoney(fromAccountId, amount)

                  rollbackTransaction().map {
                    case Left(rollbackErrorMsg) => Left(errorMessage.concat(rollbackErrorMsg))
                    case Right(_) => Left(errorMessage)
                  }
                case Right(_) => Future successful Right(ServiceSuccess("success"))
              }
          }
        case (Some(_), None) =>
          Future successful Left(Error.TargetAccountDoesntExistErrorMgs)
        case (None, Some(_)) =>
          Future successful Left(Error.SourceAccountDoesntExistErrorMsg)
      }
    }).flatMap(identity)
  }

  def findAccountById(accountId: AccountId): Future[Option[Account]] = {
    accountAccessor.ask(AccountAccessor.FindEntityById(accountId)).mapTo[Option[Account]]
  }

  def withdrawMoney(accountId: AccountId, amount: Money): Future[Either[ErrorMessage, AccountId]] = {
    accountAccessor.ask(AccountAccessor.WithdrawMoney(accountId, amount)).mapTo[Either[ErrorMessage, AccountId]]
  }

  def depositMoney(accountId: AccountId, amount: Money): Future[Either[ErrorMessage, AccountId]] = {
    accountAccessor.ask(AccountAccessor.DepositMoney(accountId, amount)).mapTo[Either[ErrorMessage, AccountId]]
  }
}


