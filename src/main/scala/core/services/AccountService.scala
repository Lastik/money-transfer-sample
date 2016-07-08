package core.services

import akka.actor.{Actor, Stash}
import akka.pattern.{ask, pipe}
import common.actors.LookupBusinessActor
import core.DefaultTimeout
import core.dal.{AccountAccessor, CustomerAccessor}
import core.model.{Account, AccountId, CustomerId}
import core.services.AccountService.{GetAccountById, GetCustomerAccounts, TransferMoney}
import squants.market.Money

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AccountService {

  val Id = "account-service"

  case class GetAccountById(accountId: AccountId)

  case class GetCustomerAccounts(customerId: CustomerId)

  case class TransferMoney(dto: TransferMoneyRequestDTO)

}

//TODO: for now, Account service can process only one transaction per time.
class AccountService extends Actor with LookupBusinessActor with DefaultTimeout with Stash {

  val customerAccessor = lookupByContext(CustomerAccessor.Id)
  val accountAccessor = lookupByContext(AccountAccessor.Id)

  var currentlyProcessingAccounts: List[AccountId] = Nil

  def receive_ProcessMoneyTransferMessages: Receive = {
    case TransferMoney(TransferMoneyRequestDTO(from, to, amount)) =>
      become_stashIncomingMoneyTransferMessages()
      val moneyTransferTransactionResult = for {
        fromAccountOpt <- findAccountById(from)
        toAccountOpt <- findAccountById(to)
      } yield {
        (fromAccountOpt, toAccountOpt) match {
          case (Some(fromAccount), Some(toAccount)) =>
            if (fromAccount.money.currency != amount.currency) {
              TransferMoneyResponseDTO(succeeded = false, "Unable to transfer money: wrong currency for amount specified")
            }
            else if (fromAccount.money < amount) {
              TransferMoneyResponseDTO(succeeded = false, "Unable to transfer money: insufficient amount of money on account")
            }
            else {
              accountAccessor ! AccountAccessor.UpdateEntityById(fromAccount.id, fromAccount.withdrawMoney(amount))
              accountAccessor ! AccountAccessor.UpdateEntityById(toAccount.id, toAccount.depositMoney(amount))

              TransferMoneyResponseDTO(succeeded = true, "Transfer successfully performed")
            }

          case (Some(_), None) =>
            TransferMoneyResponseDTO(succeeded = false, "Unable to transfer money: target account doesn't exist")
          case (None, Some(_)) =>
            TransferMoneyResponseDTO(succeeded = false, "Unable to transfer money: source account doesn't exist")
        }
      }

      moneyTransferTransactionResult.andThen {
        case _ =>
          become_processAllIncomingMessages()
      }

      moneyTransferTransactionResult pipeTo sender
  }

  def receive_StashMoneyTransferMessages: Receive = {
    case TransferMoney(_) => stash()
  }

  def receive_ProcessOthers: Receive = {
    case GetAccountById(accountId) =>
      accountAccessor.ask(AccountAccessor.GetEntityById(accountId)) pipeTo sender
    case GetCustomerAccounts(customerId) =>
      accountAccessor.ask(AccountAccessor.GetCustomerAccounts(customerId)).mapTo[List[Account]].map(AccountsDTO) pipeTo sender
  }

  def receive_All = receive_ProcessMoneyTransferMessages orElse receive_ProcessOthers

  def receive_AllExceptMoneyTransferMessages = receive_StashMoneyTransferMessages orElse receive_ProcessOthers

  override def receive = receive_All

  def become_stashIncomingMoneyTransferMessages() = {
    context.become(receive_AllExceptMoneyTransferMessages)
  }

  def become_processAllIncomingMessages() = {
    unstashAll()
    context.become(receive_All)
  }

  def findAccountById(accountId: AccountId): Future[Option[Account]] = {
    accountAccessor.ask(AccountAccessor.FindEntityById(accountId)).mapTo[Option[Account]]
  }
}

case class TransferMoneyRequestDTO(from: AccountId, to: AccountId, amount: Money)

case class TransferMoneyResponseDTO(succeeded: Boolean, message: String)

case class AccountsDTO(accounts: List[Account])

