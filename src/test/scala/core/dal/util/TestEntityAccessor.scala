package core.dal.util

import core.dal.{DataAccessor, DataAccessorProtocol}

object TestEntityAccessor extends DataAccessorProtocol{
  val Id = "test-entity-accessor"
}

class TestEntityAccessor extends DataAccessor[TestEntity, TestEntityId] {
  def receiveFun: Receive = PartialFunction.empty
}
