package demo

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import core.dal.{AccountAccessor, CustomerAccessor}
import core.services.helpers.{AccountServiceHelper, CustomerServiceHelper}
import core.services.{AccountService, CustomerService}
import util.ActorSpecBase

class DemoDataLoaderSpec extends TestKit(ActorSystem("DemoDataLoader")) with ActorSpecBase
  with CustomerServiceHelper with AccountServiceHelper {

  val accountAccessor = system.actorOf(Props(classOf[AccountAccessor], 3), AccountAccessor.Id)
  val customerAccessor = system.actorOf(Props(classOf[CustomerAccessor], 3), CustomerAccessor.Id)

  val accountService = system.actorOf(Props(classOf[AccountService]), AccountService.Id)
  val customerService = system.actorOf(Props(classOf[CustomerService]), CustomerService.Id)

  val demoDataLoader = system.actorOf(Props(classOf[DemoDataLoader]), DemoDataLoader.Id)

  "DemoDataLoader" must {

    "Be able load demo data to the system" in {

      demoDataLoader.ask(DemoDataLoader.LoadDemoData()).awaitResult

      val allCustomers = getAllCustomers.data

      allCustomers.nonEmpty shouldEqual true

      allCustomers.exists(customer => {
        findCustomerAccounts(customer.id).right.get.result.accounts.nonEmpty
      }) shouldEqual true
    }
  }
}