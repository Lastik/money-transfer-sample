package core.dal.base

import java.util.UUID

import akka.actor.ActorRef
import akka.routing.Routee
import core.model.{Account, AccountFixture, AccountId}
import org.scalatest.{FunSpec, Matchers}

class DataAccessorMessagesRoutingLogicSpec extends FunSpec with Matchers {

  import Account._

  describe("DataAccessorMessagesRoutingLogic") {

    val routeesCount = 100

    val routees = (1 to routeesCount).map(_ => DummyRoutee())

    val routingLogic = new DataAccessorMessagesRoutingLogic()

    val accountsIds = (1 to routeesCount).map(_ => AccountId()).toList

    val accounts = accountsIds.map(accountId => new AccountFixture().entity.copy(id = accountId))

    it("Route messages extending RouteMessageById to different routes for load balancing") {
      val routesForAccountsIds = accountsIds.map(accountId => {
        val message = DataAccessorWorker.FindEntityById(accountId)
        routingLogic.select(message, routees)
      })

      routesForAccountsIds.distinct.size > 1 shouldEqual true
    }

    it("Route for messages extending RouteMessageById should always be the same for same id"){

      def buildRoutingMapFromAccountIdToRoute = accountsIds.map(accountId => {
        val message = DataAccessorWorker.FindEntityById(accountId)
        (accountId, routingLogic.select(message, routees))
      }).toMap

      val routeByAccountIdMapFirstAttempt = buildRoutingMapFromAccountIdToRoute
      val routeByAccountIdMapSecondAttempt = buildRoutingMapFromAccountIdToRoute

      routeByAccountIdMapFirstAttempt.foreach({
        case (accountId, routeFromFirstAttempt) =>
          val routeFromSecondAttempt = routeByAccountIdMapSecondAttempt(accountId)
          routeFromFirstAttempt shouldEqual routeFromSecondAttempt
      })
    }

    it("Route for messages extending RouteMessageByEntity should always be the same for same id"){

      def buildRoutingMapFromAccountToRoute = accounts.map(account => {
        val message = DataAccessorWorker.CreateEntity(account)
        (account, routingLogic.select(message, routees))
      }).toMap

      val routeByAccountMapFirstAttempt = buildRoutingMapFromAccountToRoute
      val routeByAccountMapSecondAttempt = buildRoutingMapFromAccountToRoute

      buildRoutingMapFromAccountToRoute.foreach({
        case (account, routeFromFirstAttempt) =>
          val routeFromSecondAttempt = routeByAccountMapSecondAttempt(account)
          routeFromFirstAttempt shouldEqual routeFromSecondAttempt
      })
    }

    it("Route other messages"){
      val route = routingLogic.select("Some other message", routees)
      routees.contains(route) shouldEqual true
    }
  }
}

case class DummyRoutee(id: String = UUID.randomUUID().toString) extends Routee {
  override def send(message: Any, sender: ActorRef): Unit =
    throw new UnsupportedOperationException("Not supported")
}