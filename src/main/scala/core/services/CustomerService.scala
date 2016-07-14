package core.services

import akka.actor.Actor
import akka.pattern.{ask, pipe}
import common.actors.LookupBusinessActor
import core.DefaultTimeout
import core.dal.CustomerAccessor
import core.model.Customer
import core.services.CustomerService.{CreateCustomer, GetAllCustomers}

import scala.concurrent.ExecutionContext.Implicits.global

object CustomerService{

  val Id = "customer-service"

  case class CreateCustomer(customerDTO: CustomerDTO)

  case class GetAllCustomers()
}

class CustomerService extends Actor with LookupBusinessActor with DefaultTimeout {

  val customerAccessor = lookupByContext(CustomerAccessor.Id)

  override def receive: Receive = {
    case CreateCustomer(customerDTO) =>
      customerAccessor.ask(CustomerAccessor.CreateEntity(Customer.apply(customerDTO))) pipeTo sender
    case GetAllCustomers() =>
      customerAccessor.ask(CustomerAccessor.GetAllEntities()).mapTo[List[Customer]].map(CustomersDTO) pipeTo sender
  }
}

case class CustomerDTO(name: String)

case class CustomersDTO(data: List[Customer])
