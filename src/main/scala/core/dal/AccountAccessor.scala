package core.dal

import akka.actor.{ActorRef, Props}
import akka.routing.ConsistentHashingRouter.ConsistentHashable
import common.ErrorMessage
import core.dal.base.{DataAccessor, DataAccessorProtocol, DataAccessorWorker}
import core.model.{Account, AccountId}
import squants.Money

object AccountAccessor extends DataAccessorProtocol {

  val Id = "account-accessor"

  case class WithdrawMoney(accountId: AccountId, amount: Money) extends ConsistentHashable {
    def consistentHashKey = accountId
  }

  case class DepositMoney(accountId: AccountId, amount: Money) extends ConsistentHashable {
    def consistentHashKey = accountId
  }

  object Errors {

    case class AccountNotFound(accountId: AccountId) extends ErrorMessage {
      val text = s"Account with id = $accountId not found!"
    }

  }

}

class AccountAccessor(nrOfWorkers: Int) extends DataAccessor[Account, AccountId](nrOfWorkers = nrOfWorkers) {

  def receiveFun: Receive = PartialFunction.empty

  def createWorker(): ActorRef = context.actorOf(Props[AccountAccessorWorker])
}

class AccountAccessorWorker extends DataAccessorWorker[Account, AccountId] {

  import AccountAccessor._

  def receiveFun: Receive = {
    case WithdrawMoney(accountId, amount) =>
      sender ! updateAccount(accountId, account => account.withdrawMoney(amount))
    case DepositMoney(accountId, amount) =>
      sender ! updateAccount(accountId, account => account.depositMoney(amount))
  }

  def updateAccount(accountId: AccountId, updateFunc: Function[Account, Either[ErrorMessage, Account]]) = {
    findEntityById(accountId) match {
      case Some(account) =>
        updateFunc(account) match {
          case Left(errorMessage) => Left(errorMessage)
          case Right(updatedAccount) =>
            updateEntity(updatedAccount)
            Right(updatedAccount)
        }
      case None => Left(Errors.AccountNotFound(accountId))
    }
  }
}