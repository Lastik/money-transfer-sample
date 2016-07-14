package demo

import akka.actor.Actor
import akka.pattern.{ask, pipe}
import common.actors.LookupBusinessActor
import core.DefaultTimeout
import core.dal.{AccountAccessor, CustomerAccessor}
import core.model.{Account, AccountId, Customer, CustomerId}
import demo.DemoDataLoader.LoadDemoData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DemoDataLoader {
  val Id = "demo-data-loader"

  case class LoadDemoData()
}

class DemoDataLoader extends Actor with LookupBusinessActor with DefaultTimeout {

  val customerAccessor = lookupByContext(CustomerAccessor.Id)
  val accountAccessor = lookupByContext(AccountAccessor.Id)

  override def receive: Receive = {
    case LoadDemoData() =>
      (for {
        customersIds <- Future.sequence(DemoCustomers.Values.map {
          customer => createCustomer(customer)
        })

        accountsIds <- Future.sequence(DemoAccounts.Values.map {
          account => createAccount(account)
        })

      } yield true) pipeTo sender
  }

  def createCustomer(customer: Customer): Future[CustomerId] = {
    customerAccessor.ask(CustomerAccessor.CreateEntity(customer)).mapTo[CustomerId]
  }

  def createAccount(account: Account): Future[AccountId] = {
    accountAccessor.ask(AccountAccessor.CreateEntity(account)).mapTo[AccountId]
  }
}