package core

import akka.actor.{ActorSystem, Props}
import core.dal.{AccountAccessor, CustomerAccessor}
import core.services.{AccountService, CustomerService, TransactionService}
import demo.DemoDataLoader
import demo.DemoDataLoader.LoadDemoData

/**
 * Core is type containing the ``system: ActorSystem`` member. This enables us to use it in our
 * apps as well as in our tests.
 */
trait Core {

  implicit def system: ActorSystem

}

/**
 * This trait implements ``Core`` by starting the required ``ActorSystem`` and registering the
 * termination handler to stop the system when the JVM exits.
 */
trait BootedCore extends Core {

  /**
   * Construct the ActorSystem we will use in our application
   */
  implicit lazy val system = ActorSystem("akka-spray")

  /**
   * Ensure that the constructed ActorSystem is shut down when the JVM shuts down
   */
  sys.addShutdownHook(system.shutdown())

}

/**
 * This trait contains the actors that make up our application; it can be mixed in with
 * ``BootedCore`` for running code or ``TestKit`` for unit and integration tests.
 */
trait CoreActors {
  this: Core =>

  val accountAccessor    = system.actorOf(Props(classOf[AccountAccessor]), AccountAccessor.Id)
  val customerAccessor    = system.actorOf(Props(classOf[CustomerAccessor]), CustomerAccessor.Id)

  val customerService    = system.actorOf(Props(classOf[CustomerService]), CustomerService.Id)
  val accountService    = system.actorOf(Props(classOf[AccountService]), AccountService.Id)
  val transactionService    = system.actorOf(Props(classOf[TransactionService]), TransactionService.Id)

  val demoDataLoader = system.actorOf(Props(classOf[DemoDataLoader]), DemoDataLoader.Id)
  demoDataLoader ! LoadDemoData()

}