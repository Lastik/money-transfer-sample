package core.dal

import core.model.{Customer, CustomerId}

object CustomerAccessor extends DataAccessorProtocol{
  val Id = "customer-accessor"
}

class CustomerAccessor extends DataAccessor[Customer, CustomerId] {

  var entitiesMap = Customer.Values.map(customer => (customer.id, customer)).toMap

  def updateEntity(entityId: CustomerId, newValue: Customer): Unit = {
    entitiesMap = entitiesMap + (entityId -> newValue)
  }

  def receiveFun: Receive = PartialFunction.empty
}
