package core.dal

import akka.actor.Actor
import core.model.{ModelEntity, ModelEntityKey}

import scala.collection.mutable

trait DataAccessorProtocol {

  case class GetAllEntities()

  case class FindEntityById[KeyType <: ModelEntityKey](id: KeyType) extends DataAccessorMessageWithId[KeyType]

  case class GetEntityById[KeyType <: ModelEntityKey](id: KeyType) extends DataAccessorMessageWithId[KeyType]

  case class CheckIfEntityExistsById[KeyType <: ModelEntityKey](id: KeyType) extends DataAccessorMessageWithId[KeyType]

  case class CreateEntity[EntityType <: ModelEntity](entity: EntityType) extends DataAccessorMessageWithEntity[EntityType]

}

object DataAccessorWorker extends DataAccessorProtocol

abstract class DataAccessorWorker[EntityType <: ModelEntity, KeyType <: ModelEntityKey] extends Actor {

  import DataAccessorWorker._

  private var entitiesMap = mutable.Map[KeyType, EntityType]()

  def receiveFun: Receive

  def receiveBase: Receive = {
    case GetAllEntities() =>
      sender ! entitiesMap.values.toList
    case FindEntityById(id) =>
      sender ! findEntityById(id.asInstanceOf[KeyType])
    case GetEntityById(id) =>
      sender ! findEntityById(id.asInstanceOf[KeyType]).getOrElse(throw new IllegalArgumentException("Entity with specified id not found"))
    case CreateEntity(entity) =>
      createEntity(entity = entity.asInstanceOf[EntityType])
      sender ! entity.id
    case CheckIfEntityExistsById(id) =>
      sender ! entitiesMap.contains(id.asInstanceOf[KeyType])
  }

  def receive = receiveBase orElse receiveFun

  def createEntity(entity: EntityType) {
    val entityId = entity.id.asInstanceOf[KeyType]
    if (entitiesMap.contains(entityId)) {
      throw new IllegalArgumentException("Attempt to insert entity which already exists")
    }
    entitiesMap(entity.id.asInstanceOf[KeyType]) = entity
  }

  def updateEntity(entity: EntityType) {
    val entityId = entity.id.asInstanceOf[KeyType]
    if (!entitiesMap.contains(entityId)) {
      throw new IllegalArgumentException("Attempt to update non existed entity")
    }
    entitiesMap(entity.id.asInstanceOf[KeyType]) = entity
  }

  def getEntityById(id: KeyType) = {
    entitiesMap(id)
  }

  def findEntityById(id: KeyType) = {
    entitiesMap.get(id)
  }

  def getAllEntities =
    entitiesMap.values
}