package core.model

import java.util.UUID

import core.services.CustomerDTO

case class CustomerId(id: String =  UUID.randomUUID().toString) extends ModelEntityKey

object CustomerId{
  implicit def fromUUID(uuid: UUID): CustomerId = {
    CustomerId(id = uuid.toString)
  }
}

case class Customer(id: CustomerId = CustomerId(), name: String) extends ModelEntity{
  type KeyType = CustomerId
}

object Customer {
  def apply(customerDTO: CustomerDTO): Customer = {
    Customer(name = customerDTO.name)
  }
}