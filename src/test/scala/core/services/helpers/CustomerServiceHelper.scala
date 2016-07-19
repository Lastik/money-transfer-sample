package core.services.helpers

import akka.actor.ActorRef
import akka.pattern.ask
import akka.testkit.DefaultTimeout
import akka.util.Timeout
import common.{ErrorMessage, ServiceSuccess}
import core.model.{AccountId, CustomerId}
import core.services.{CustomerCreateDTO, CustomerService, CustomersDTO}
import util.{ActorSpecBase, AwaitHelper}

import scala.concurrent.Promise

trait CustomerServiceHelper {
  this: AwaitHelper =>

  implicit def timeout: Timeout

  def customerService: ActorRef

  def createCustomer(name: String) = {
    customerService.ask(CustomerService.CreateCustomer(CustomerCreateDTO(name))).mapTo[CustomerId].awaitResult
  }

  implicit class CreateCustomerResultExtensions(createCustomerResult: CustomerId) {
    def saveToPromise(promise: Promise[CustomerId]): CustomerId = {
      promise.success(createCustomerResult)
      createCustomerResult
    }
  }

  def getAllCustomers = {
    customerService.ask(CustomerService.GetAllCustomers()).mapTo[CustomersDTO].awaitResult
  }

}
