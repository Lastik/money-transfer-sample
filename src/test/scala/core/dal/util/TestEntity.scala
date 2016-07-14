package core.dal.util

import java.util.UUID

import core.model.{ModelEntity, ModelEntityKey}

case class TestEntityId(id: String =  UUID.randomUUID().toString) extends ModelEntityKey

case class TestEntity(id: TestEntityId = TestEntityId()) extends ModelEntity {
  type KeyType = TestEntityId
}
