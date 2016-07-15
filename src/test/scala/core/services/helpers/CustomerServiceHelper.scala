package core.services.helpers

import akka.actor.ActorRef
import akka.pattern.ask
import core.model.CustomerId
import core.services.{CustomerDTO, CustomerService, CustomersDTO}
import util.ActorSpecBase

trait CustomerServiceHelper {
  this: ActorSpecBase =>

  def customerService: ActorRef

  def createCustomer(name: String) = {
    customerService.ask(CustomerService.CreateCustomer(CustomerDTO(name))).mapTo[CustomerId].awaitResult
  }

  def getAllCustomers = {
    customerService.ask(CustomerService.GetAllCustomers()).mapTo[CustomersDTO].awaitResult
  }

}
