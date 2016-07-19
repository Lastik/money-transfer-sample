package core.dal.util

import akka.actor.{ActorRef, Props}
import core.dal.{DataAccessor, DataAccessorProtocol, DataAccessorWorker}

object TestEntityAccessor extends DataAccessorProtocol{
  val Id = "test-entity-accessor"
}

class TestEntityAccessorWorker extends DataAccessorWorker[TestEntity, TestEntityId] {
  def receiveFun: Receive = PartialFunction.empty
}

class TestEntityAccessor(nrOfWorkers: Int) extends DataAccessor[TestEntity, TestEntityId](nrOfWorkers) {
  def receiveFun: Receive = PartialFunction.empty

  def createWorker(): ActorRef = context.actorOf(Props[TestEntityAccessorWorker])
}
