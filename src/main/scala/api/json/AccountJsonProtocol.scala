package api.json

import core.model.{Account, AccountId}
import core.services.{AccountDTO, AccountsDTO}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat}
import squants.market.Money

trait AccountJsonProtocol {
  this: DefaultJsonProtocol with CustomerJsonProtocol =>

  implicit val accountIdJsonFormat = new ModelEntityKeyJsonFormat[AccountId]

  implicit val moneyJsonFormat = new JsonFormat[Money] {
    def write(obj: Money): JsValue =
      JsString(obj.toString())

    def read(json: JsValue): Money = {
      json match {
        case JsString(str) =>
          Money.apply(str).getOrElse(throw new DeserializationException("Failed to deserialize Money"))
        case _ => throw new DeserializationException("Failed to deserialize Money")
      }
    }
  }

  import Account._

  implicit val accountJsonFormat = jsonFormat(Account.apply, "id", "customerId", "balance")

  implicit val accountDTOJsonFormat = jsonFormat2(AccountDTO.apply)

  implicit val accountsDTOJsonFormat = jsonFormat1(AccountsDTO.apply)
}
