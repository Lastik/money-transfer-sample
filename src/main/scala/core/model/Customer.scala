package core.model

import java.util.UUID

import core.services.CustomerCreateDTO

case class CustomerId(id: String =  UUID.randomUUID().toString) extends ModelEntityKey

object CustomerId{
  implicit def fromUUID(uuid: UUID): CustomerId = {
    CustomerId(id = uuid.toString)
  }
}

case class Customer(id: CustomerId = CustomerId(), name: String, accounts: List[AccountId] = Nil) extends ModelEntity {
  type KeyType = CustomerId

  def linkWithAccount(accountId: AccountId) = {
    this.copy(accounts = accountId :: accounts)
  }
}

object Customer {
  def apply(customerCreateDTO: CustomerCreateDTO): Customer = {
    Customer(name = customerCreateDTO.name)
  }
}