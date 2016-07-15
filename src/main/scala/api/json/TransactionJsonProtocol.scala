package api.json

import spray.json._
import core.model.{AccountId, Transaction}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsObject, JsValue, RootJsonFormat}
import squants.Money

trait TransactionJsonProtocol {
  this: DefaultJsonProtocol with AccountJsonProtocol with CommonJsonProtocol =>

  implicit val transactionJsonFormat = new RootJsonFormat[Transaction] {

    override def write(transaction: Transaction): JsValue = {
      JsObject(
        "from" -> transaction.from.toJson,
        "to" -> transaction.to.toJson,
        "amount" -> transaction.amount.toJson
      )
    }

    override def read(json: JsValue): Transaction = {
      json match {
        case jsObject: JsObject =>
          Transaction(
            from = jsObject.fields("from").convertTo[AccountId],
            to = jsObject.fields("to").convertTo[AccountId],
            amount = jsObject.fields("amount").convertTo[Money]
          )
        case _ => throw new DeserializationException("")
      }
    }
  }

}