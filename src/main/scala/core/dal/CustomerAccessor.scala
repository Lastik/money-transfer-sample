package core.dal

import core.model.{Customer, CustomerId}

object CustomerAccessor extends DataAccessorProtocol{
  val Id = "customer-accessor"
}

class CustomerAccessor extends DataAccessor[Customer, CustomerId] {
  def receiveFun: Receive = PartialFunction.empty
}