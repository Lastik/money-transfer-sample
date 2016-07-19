package core.dal

import _root_.util.ActorSpecBase
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import core.dal.util.{TestEntity, TestEntityAccessor, TestEntityId}

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Promise

class DataAccessorSpec extends TestKit(ActorSystem("DataAccessor")) with ActorSpecBase {

  val testEntityAccessor = system.actorOf(Props(classOf[TestEntityAccessor], 3), TestEntityAccessor.Id)

  val testEntityPromise: Promise[TestEntity] = Promise()

  def testEntityIdFtr = testEntityPromise.future.map(_.id)

  "DataAccessor" must {

    "Be able to create new test entity" in {

      val testEntityToCreate = TestEntity()

      val createdTestEntityId = createTestEntity(testEntityToCreate)

      createdTestEntityId shouldEqual testEntityToCreate.id
      testEntityPromise.success(testEntityToCreate)
    }

    "Be able to find existing test entity by id" in {
      val testEntityId = testEntityIdFtr.awaitResult
      val findTestEntityResult = testEntityAccessor.ask(TestEntityAccessor.FindEntityById(testEntityId)).awaitResult

      findTestEntityResult match{
        case Some(testEntity: TestEntity) =>
          testEntity.id shouldEqual testEntityId
        case _  => fail()
      }
    }

    "Not be able to non existed test entity by id" in {
      val findTestEntityResult = testEntityAccessor.ask(TestEntityAccessor.FindEntityById(TestEntityId())).awaitResult

      findTestEntityResult match{
        case None =>
        case _  => fail()
      }
    }

    "Be be able to get existed test entity by id" in {
      val testEntityId = testEntityIdFtr.awaitResult
      val getTestEntityResult = testEntityAccessor.ask(TestEntityAccessor.GetEntityById(testEntityId)).awaitResult

      getTestEntityResult match {
        case testEntity: TestEntity =>
          testEntity.id shouldEqual testEntityId
        case _ => fail()
      }
    }

    "Be be able to get all existing test entities" in {
      val testEntityId = testEntityIdFtr.awaitResult
      val otherTestEntityId = createTestEntity(TestEntity())

      val getAllTestEntitiesResult = testEntityAccessor.ask(TestEntityAccessor.GetAllEntities()).awaitResult

      getAllTestEntitiesResult match{
        case listOfTestEntities: List[Any] if listOfTestEntities.forall(_.isInstanceOf[TestEntity]) =>
          listOfTestEntities.length shouldEqual 2
        case _ => fail()
      }
    }

    "be able to check if entity exists by it's id" in {
      val testEntityId = testEntityIdFtr.awaitResult

      val existsRes =  testEntityAccessor.ask(TestEntityAccessor.CheckIfEntityExistsById(testEntityId)).awaitResult

      existsRes match {
        case exists: Boolean =>
          exists shouldEqual true
        case _ => fail()
      }
    }

    "be able to check if entity doesn't exists by id" in {

      val existsRes =  testEntityAccessor.ask(TestEntityAccessor.CheckIfEntityExistsById(TestEntityId())).awaitResult

      existsRes match {
        case exists: Boolean =>
          exists shouldEqual false
        case _ => fail()
      }
    }
  }

  def createTestEntity(testEntity: TestEntity): TestEntityId = {
    testEntityAccessor.ask(TestEntityAccessor.CreateEntity(testEntity)).mapTo[TestEntityId].awaitResult
  }
}