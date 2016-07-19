package core.dal.base

import akka.actor.{Actor, ActorRef}
import akka.pattern.{ask, pipe}
import akka.routing.{ActorRefRoutee, ConsistentHashingRoutingLogic, Router}
import core.DefaultTimeout
import core.model.{ModelEntity, ModelEntityKey}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class DataAccessor[EntityType <: ModelEntity, KeyType <: ModelEntityKey](nrOfWorkers: Int) extends Actor with DefaultTimeout {

  import DataAccessorWorker._

  def createWorker(): ActorRef

  val workers = (1 to nrOfWorkers).map(_ => createWorker())

  val router = Router(new ConsistentHashingRoutingLogic(system = context.system),
    workers.map(worker => ActorRefRoutee(worker)))

  def receiveFun: Receive

  def receiveBase: Receive = {
    case GetAllEntities() =>
      Future.sequence(workers.map(worker =>
        worker.ask(GetAllEntities()).mapTo[List[ModelEntity]])).map(res => {
        res.flatten.toList
      }) pipeTo sender
    case message => router.route(message, sender)
  }

  def receive = receiveBase orElse receiveFun
}