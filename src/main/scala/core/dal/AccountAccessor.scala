package core.dal

import common.ErrorMessage
import core.model.{Account, AccountId, CustomerId}
import squants.Money

object AccountAccessor extends DataAccessorProtocol {

  val Id = "account-accessor"

  case class GetCustomerAccounts(customerId: CustomerId)

  case class WithdrawMoney(accountId: AccountId, amount: Money)

  case class DepositMoney(accountId: AccountId, amount: Money)

  object Errors {

    case class AccountNotFound(accountId: AccountId) extends ErrorMessage {
      val text = s"Account with id = $accountId not found!"
    }

  }

}

class AccountAccessor extends DataAccessor[Account, AccountId] {

  import AccountAccessor._

  def receiveFun: Receive = {
    case GetCustomerAccounts(customerId) =>
      sender ! getAllEntities.filter(_.customerId == customerId)
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