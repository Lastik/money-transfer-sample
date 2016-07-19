package core.services

import akka.actor.Actor
import akka.pattern.{ask, pipe}
import common.actors.LookupBusinessActor
import core.DefaultTimeout
import core.dal.CustomerAccessor
import core.model.{Customer, CustomerId}
import core.services.CustomerService.{CreateCustomer, GetAllCustomers}

import scala.concurrent.ExecutionContext.Implicits.global

object CustomerService{

  val Id = "customer-service"

  case class CreateCustomer(customerDTO: CustomerCreateDTO)

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

  def createCustomer(customerDTO: CustomerCreateDTO) = {
    customerAccessor.ask(CustomerAccessor.CreateEntity(Customer.apply(customerDTO))).mapTo[CustomerId]
  }

  def getAllCustomers = {
    customerAccessor.ask(CustomerAccessor.GetAllEntities()).mapTo[List[Customer]].map(
      customers => CustomersDTO.apply(customers.map(CustomerIdNamePair.fromCustomer)))
  }
}

case class CustomerCreateDTO(name: String)

case class CustomerIdNamePair(id: CustomerId, name: String)

object CustomerIdNamePair {
  def fromCustomer(customer: Customer) = {
    CustomerIdNamePair(id = customer.id, name = customer.name)
  }
}

case class CustomersDTO(data: List[CustomerIdNamePair])
