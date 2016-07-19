package core.services.helpers

import akka.actor.ActorRef
import akka.pattern.ask
import akka.testkit.DefaultTimeout
import akka.util.Timeout
import core.model.CustomerId
import core.services.{CustomerCreateDTO, CustomerService, CustomersDTO}
import util.{ActorSpecBase, AwaitHelper}

trait CustomerServiceHelper {
  this: AwaitHelper =>

  implicit def timeout: Timeout

  def customerService: ActorRef

  def createCustomer(name: String) = {
    customerService.ask(CustomerService.CreateCustomer(CustomerCreateDTO(name))).mapTo[CustomerId].awaitResult
  }

  def getAllCustomers = {
    customerService.ask(CustomerService.GetAllCustomers()).mapTo[CustomersDTO].awaitResult
  }

}
