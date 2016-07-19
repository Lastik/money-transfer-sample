package common

import java.util.concurrent.atomic.AtomicLong

import akka.routing._
import core.dal.{DataAccessorMessageWithEntity, DataAccessorMessageWithId}
import core.model.ModelEntityKey

import scala.collection.immutable

@SerialVersionUID(1L)
final class ModelEntityKeyAsRouteRoutingLogic extends RoutingLogic {

  override def select(message: Any, routees: immutable.IndexedSeq[Routee]): Routee = {

    val next = new AtomicLong(0)

    message match {
      case dalMessageWithKey: DataAccessorMessageWithId[_] =>
        routeByModelEntityId(dalMessageWithKey.id, routees)
      case dalMessageWithEntity: DataAccessorMessageWithEntity[_] =>
        routeByModelEntityId(dalMessageWithEntity.entity.id, routees)
      case _ => routees((next.getAndIncrement % routees.size).asInstanceOf[Int])
    }
  }

  def routeByModelEntityId[KeyType <: ModelEntityKey](id: KeyType, routees: immutable.IndexedSeq[Routee]) = {
    val hash = Math.abs(id.toString.hashCode)

    val actorIndex = hash % routees.length

    routees(actorIndex)
  }
}