package core.dal

import akka.actor.{Actor, ActorRef}
import core.model.{ModelEntity, ModelEntityKey}
import akka.pattern.{ask, pipe}
import akka.routing.{ActorRefRoutee, Router}
import common.ModelEntityKeyAsRouteRoutingLogic
import core.DefaultTimeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class DataAccessor[EntityType <: ModelEntity, KeyType <: ModelEntityKey](nrOfWorkers: Int) extends Actor with DefaultTimeout {

  import DataAccessorWorker._

  def createWorker(): ActorRef

  val workers = (1 to nrOfWorkers).map(_ => createWorker())

  val router = {
    val routees = workers.map(worker => ActorRefRoutee(worker))
    Router(new ModelEntityKeyAsRouteRoutingLogic(), routees)
  }

  def receiveFun: Receive

  def receiveBase: Receive = {
    case GetAllEntities() =>
      Future.sequence(workers.map(worker =>
        worker.ask(GetAllEntities()).mapTo[List[ModelEntity]])).map(res => {
        res.flatten.toList
      }) pipeTo sender
    case dalMessageWithId: DataAccessorMessageWithId[_] =>
      router.route(dalMessageWithId, sender)
    case dalMessageWithEntity: DataAccessorMessageWithEntity[_] =>
      router.route(dalMessageWithEntity, sender)
  }

  def receive = receiveBase orElse receiveFun
}

trait DataAccessorMessageWithId[KeyType <: ModelEntityKey] {
  def id: KeyType
}

trait DataAccessorMessageWithEntity[EntityType <: ModelEntity] {
  def entity: EntityType
}