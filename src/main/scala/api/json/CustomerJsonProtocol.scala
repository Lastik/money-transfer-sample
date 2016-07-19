package api.json

import core.model.{Customer, CustomerId}
import core.services.{CustomerIdNamePair, CustomerCreateDTO, CustomersDTO}
import spray.json.DefaultJsonProtocol

trait CustomerJsonProtocol {
  this: DefaultJsonProtocol with CommonJsonProtocol =>

  implicit val customerIdJsonFormat = new ModelEntityKeyJsonFormat[CustomerId]

  implicit val customerIdNamePairJsonFormat = jsonFormat2(CustomerIdNamePair.apply)

  implicit val customerCreateDTOJsonFormat = jsonFormat1(CustomerCreateDTO.apply)

  implicit val customersDTOJsonFormat = jsonFormat1(CustomersDTO.apply)

}
