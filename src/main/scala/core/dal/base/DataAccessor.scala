package core.dal.base

import akka.actor.{Actor, ActorRef}
import akka.pattern.{ask, pipe}
import akka.routing.{ActorRefRoutee, Router}
import core.DefaultTimeout
import core.model.{ModelEntity, ModelEntityKey}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class DataAccessor[EntityType <: ModelEntity, KeyType <: ModelEntityKey](nrOfWorkers: Int) extends Actor with DefaultTimeout {

  import DataAccessorWorker._

  def createWorker(): ActorRef

  val workers = (1 to nrOfWorkers).map(_ => createWorker())

  val router = Router(new DataAccessorMessagesRoutingLogic(), workers.map(worker => ActorRefRoutee(worker)))

  def receiveFun: Receive

  def receiveBase: Receive = {
    case GetAllEntities() =>
      Future.sequence(workers.map(worker =>
        worker.ask(GetAllEntities()).mapTo[List[ModelEntity]])).map(res => {
        res.flatten.toList
      }) pipeTo sender
    case routeMessageById: RouteMessageById[_] =>
      router.route(routeMessageById, sender)
    case routeMessageByEntity: RouteMessageByEntity[_] =>
      router.route(routeMessageByEntity, sender)
  }

  def receive = receiveBase orElse receiveFun
}

trait RouteMessageById[KeyType <: ModelEntityKey] {
  def id: KeyType
}

trait RouteMessageByEntity[EntityType <: ModelEntity] {
  def entity: EntityType
}