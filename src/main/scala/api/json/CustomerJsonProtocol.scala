package api.json

import core.model.{AccountId, Customer, CustomerId}
import core.services.CustomersDTO
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat}

trait CustomerJsonProtocol {
  this: DefaultJsonProtocol =>

  implicit val customerIdJsonFormat = new ModelEntityKeyJsonFormat[CustomerId]

  implicit val customerJsonFormat = jsonFormat2(Customer.apply)

  implicit val customersDTOJsonFormat = jsonFormat1(CustomersDTO.apply)

}
