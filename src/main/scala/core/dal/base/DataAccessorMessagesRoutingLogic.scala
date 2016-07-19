package core.dal.base

import java.util.concurrent.atomic.AtomicLong

import akka.routing._
import core.model.ModelEntityKey

import scala.collection.immutable

@SerialVersionUID(1L)
final class DataAccessorMessagesRoutingLogic extends RoutingLogic {

  val next = new AtomicLong(0)

  override def select(message: Any, routees: immutable.IndexedSeq[Routee]): Routee = {
    message match {
      case routeMessageById: RouteMessageById[_] =>
        routeByModelEntityId(routeMessageById.id, routees)
      case dalMessageByEntity: RouteMessageByEntity[_] =>
        routeByModelEntityId(dalMessageByEntity.entity.id, routees)
      case _ => routees((next.getAndIncrement % routees.size).asInstanceOf[Int])
    }
  }

  def routeByModelEntityId[KeyType <: ModelEntityKey](id: KeyType, routees: immutable.IndexedSeq[Routee]) = {
    val hash = id.toString.hashCode

    val actorIndex = Math.abs(hash) % routees.length

    routees(actorIndex)
  }
}