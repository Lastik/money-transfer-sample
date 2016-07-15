package core.services

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import core.dal.CustomerAccessor
import core.model.CustomerId
import core.services.helpers.CustomerServiceHelper
import util.ActorSpecBase

import scala.concurrent.Promise

class CustomerServiceSpec extends TestKit(ActorSystem("CustomerService")) with ActorSpecBase
with CustomerServiceHelper {

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

      val allCustomers = getAllCustomers

      allCustomers.data.length shouldEqual 2

      allCustomers.data.map(_.id).toSet shouldEqual Set(customerId, otherCustomerId)
    }

  }
}