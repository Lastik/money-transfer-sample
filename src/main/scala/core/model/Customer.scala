package core.model

import java.util.UUID

case class CustomerId(id: String =  UUID.randomUUID().toString) extends ModelEntityKey

object CustomerId{
  implicit def fromUUID(uuid: UUID) = {
    CustomerId(id = uuid.toString)
  }
}

case class Customer(id: CustomerId = CustomerId(), name: String) extends ModelEntity

object Customer {

  val VasyaPupkin = Customer(name = "Vasya Pupkin")
  val IvanIvanov = Customer(name = "Ivan Ivanov")

  val Values = List(VasyaPupkin, IvanIvanov)
}
