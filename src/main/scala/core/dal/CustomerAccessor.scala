package core.dal

import akka.actor.{ActorRef, Props}
import common.ErrorMessage
import core.dal.base.{DataAccessor, RouteMessageById, DataAccessorProtocol, DataAccessorWorker}
import core.model.{AccountId, Customer, CustomerId}

object CustomerAccessor extends DataAccessorProtocol {
  val Id = "customer-accessor"

  case class GetCustomerAccounts(customerId: CustomerId) extends RouteMessageById[CustomerId] {
    def id = customerId
  }

  case class LinkCustomerWithAccount(customerId: CustomerId, accountId: AccountId)
    extends RouteMessageById[CustomerId] {
    def id = customerId
  }

  object Errors {

    case class CustomerNotFound(customerId: CustomerId) extends ErrorMessage {
      val text = s"Customer with id = $customerId not found!"
    }

  }

}

class CustomerAccessor(nrOfWorkers: Int) extends DataAccessor[Customer, CustomerId](nrOfWorkers = nrOfWorkers) {
  def receiveFun: Receive = PartialFunction.empty

  def createWorker(): ActorRef = context.actorOf(Props[CustomerAccessorWorker])
}

class CustomerAccessorWorker extends DataAccessorWorker[Customer, CustomerId] {

  import CustomerAccessor._

  def receiveFun: Receive = {
    case GetCustomerAccounts(customerId) =>
      sender ! getEntityById(customerId).accounts
    case LinkCustomerWithAccount(customerId, accountId) =>
      sender ! (findEntityById(customerId) match {
        case Some(customer) =>
          val updatedCustomer = customer.linkWithAccount(accountId)
          updateEntity(updatedCustomer)
          Right(updatedCustomer)
        case None => Left(Errors.CustomerNotFound(customerId))
      })
  }
}