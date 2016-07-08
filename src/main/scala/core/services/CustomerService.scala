package core.services

import akka.actor.Actor
import akka.pattern.{ask, pipe}
import common.actors.LookupBusinessActor
import core.DefaultTimeout
import core.dal.CustomerAccessor
import core.model.Customer
import core.services.CustomerService.GetAllCustomers

import scala.concurrent.ExecutionContext.Implicits.global

object CustomerService{

  val Id = "customer-service"

  case class GetAllCustomers()
}

class CustomerService extends Actor with LookupBusinessActor with DefaultTimeout {

  val customerAccessor = lookupByContext(CustomerAccessor.Id)

  override def receive: Receive = {
    case GetAllCustomers() =>
      customerAccessor.ask(CustomerAccessor.GetAllEntities()).mapTo[List[Customer]].map(CustomersDTO) pipeTo sender
  }
}

case class CustomersDTO(data: List[Customer])
