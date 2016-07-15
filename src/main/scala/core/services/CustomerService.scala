package core.services

import akka.actor.Actor
import akka.pattern.{ask, pipe}
import common.ErrorMessage
import common.actors.LookupBusinessActor
import core.DefaultTimeout
import core.dal.CustomerAccessor
import core.model.{Customer, CustomerId}
import core.services.CustomerService.{CreateCustomer, GetAllCustomers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CustomerService{

  val Id = "customer-service"

  case class CreateCustomer(customerDTO: CustomerDTO)

  case class GetAllCustomers()
}

class CustomerService extends Actor with LookupBusinessActor with DefaultTimeout {

  val customerAccessor = lookupByContext(CustomerAccessor.Id)

  override def receive: Receive = {
    case CreateCustomer(customerDTO) =>
      createCustomer(customerDTO) pipeTo sender
    case GetAllCustomers() =>
      getAllCustomers pipeTo sender
  }

  def createCustomer(customerDTO: CustomerDTO) = {
    customerAccessor.ask(CustomerAccessor.CreateEntity(Customer.apply(customerDTO))).mapTo[CustomerId]
  }

  def getAllCustomers = {
    customerAccessor.ask(CustomerAccessor.GetAllEntities()).mapTo[List[Customer]].map(CustomersDTO.apply)
  }
}

case class CustomerDTO(name: String)

case class CustomersDTO(data: List[Customer])
