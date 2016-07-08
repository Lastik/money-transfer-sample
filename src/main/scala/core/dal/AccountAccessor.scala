package core.dal

import core.model.{Account, AccountId, CustomerId}

object AccountAccessor extends DataAccessorProtocol{

  val Id = "account-accessor"

  case class GetCustomerAccounts(customerId: CustomerId)

}

class AccountAccessor extends DataAccessor[Account, AccountId] {

  import AccountAccessor._

  var entitiesMap = Account.Values.map(account => (account.id, account)).toMap

  def updateEntity(entityId: AccountId, newValue: Account) {
    entitiesMap = entitiesMap + (entityId -> newValue)
  }

  def receiveFun: Receive = {

    case GetCustomerAccounts(customerId) => {
      sender ! entitiesMap.values.filter(_.customerId == customerId)
    }
  }
}