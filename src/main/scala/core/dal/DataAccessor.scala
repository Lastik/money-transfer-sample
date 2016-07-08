package core.dal

import akka.actor.Actor
import core.model.{ModelEntity, ModelEntityKey}

trait DataAccessorProtocol {

  case class GetAllEntities()

  case class FindEntityById[KeyType >: ModelEntityKey](id: KeyType)

  case class GetEntityById[KeyType >: ModelEntityKey](id: KeyType)

  case class UpdateEntityById[EntityType <: ModelEntity, KeyType <: ModelEntityKey](id: KeyType, newValue: EntityType)

}

object DataAccessor extends DataAccessorProtocol

abstract class DataAccessor[EntityType <: ModelEntity, KeyType <: ModelEntityKey] extends Actor {

  import DataAccessor._

  def entitiesMap: Map[KeyType, EntityType]

  def updateEntity(entityId: KeyType, newValue: EntityType)

  def receiveFun: Receive

  def receiveBase: Receive = {
    case GetAllEntities() =>
      sender ! entitiesMap.values.toList
    case FindEntityById(id) =>
      sender ! findEntityById(id.asInstanceOf[KeyType])
    case GetEntityById(id) =>
      sender ! findEntityById(id.asInstanceOf[KeyType]).getOrElse(throw new IllegalArgumentException("Entity with specified id not found"))
    case UpdateEntityById(id, newValue) =>
      updateEntity(entityId = id.asInstanceOf[KeyType], newValue = newValue.asInstanceOf[EntityType])
      sender ! true
  }

  def findEntityById(id: KeyType) = {
    entitiesMap.get(id)
  }

  def receive = receiveBase orElse receiveFun
}
