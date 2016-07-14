package core.services

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestKit
import common.ErrorMessage
import core.dal.{AccountAccessor, CustomerAccessor}
import core.model.{Account, AccountFixture, AccountId, CustomerId}
import util.ActorSpecBase

import akka.pattern.ask

import scala.concurrent.Promise

class CustomerServiceSpec extends TestKit(ActorSystem("CustomerService")) with ActorSpecBase {

  val customerAccessor = system.actorOf(Props(classOf[CustomerAccessor]), CustomerAccessor.Id)
  val customerService = system.actorOf(Props(classOf[CustomerService]), CustomerService.Id)

  val customerIdPromise: Promise[CustomerId] = Promise()

  "CustomerService" must {

    "Be able to create customer" in {

      val customerId = createCustomer("Some customer")

      customerIdPromise.success(customerId)
    }

    "Be able find all customers" in {
      val customerId = customerIdPromise.future.awaitResult
      val otherCustomerId = createCustomer("Some customer 2")

      val allCustomers = customerService.ask(CustomerService.GetAllCustomers()).mapTo[CustomersDTO].awaitResult

      allCustomers.data.length shouldEqual 2

      allCustomers.data.map(_.id).toSet shouldEqual Set(customerId, otherCustomerId)
    }

  }

  def createCustomer(name: String) = {
    customerService.ask(CustomerService.CreateCustomer(CustomerDTO(name))).mapTo[CustomerId].awaitResult
  }
}

