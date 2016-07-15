package api.json

import core.model.{Customer, CustomerId}
import core.services.{CustomerDTO, CustomersDTO}
import spray.json.DefaultJsonProtocol

trait CustomerJsonProtocol {
  this: DefaultJsonProtocol with CommonJsonProtocol =>

  implicit val customerIdJsonFormat = new ModelEntityKeyJsonFormat[CustomerId]

  implicit val customerJsonFormat = jsonFormat2(Customer.apply)

  implicit val customerDTOJsonFormat = jsonFormat1(CustomerDTO.apply)

  implicit val customersDTOJsonFormat = jsonFormat1(CustomersDTO.apply)

}
