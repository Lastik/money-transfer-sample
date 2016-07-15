package api.json

import common.{ErrorMessage, ServiceSuccess}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsBoolean, JsFalse, JsObject, JsString, JsTrue, JsValue, JsonFormat, RootJsonFormat}

trait CommonJsonProtocol {
  this: DefaultJsonProtocol =>

  implicit def serviceSuccessJsonFormat[A](implicit format: JsonFormat[A]) = new RootJsonFormat[ServiceSuccess[A]] {

    override def write(value: ServiceSuccess[A]): JsValue = {
      JsObject("ok" -> JsBoolean(true), "result" -> format.write(value.result))
    }

    override def read(json: JsValue): ServiceSuccess[A] = {
      val root = json.asJsObject
      (root.fields.get("ok"), root.fields.get("result")) match {
        case (Some(JsTrue), Some(jsValue)) => ServiceSuccess(format.read(jsValue))

        case _ => throw new DeserializationException("JSON not a ServiceSuccess")
      }
    }
  }

  implicit object errorMessageJsonFormat extends RootJsonFormat[ErrorMessage] {

    override def write(value: ErrorMessage): JsValue = {
      JsObject("ok" -> JsBoolean(false), "error" -> JsString(value.text))
    }

    override def read(json: JsValue): ErrorMessage = {
      val root = json.asJsObject
      (root.fields.get("ok"), root.fields.get("error")) match {
        case (Some(JsFalse), Some(JsString(errorText))) => new ErrorMessage {
          val text = errorText
        }

        case _ => throw new DeserializationException("JSON not a ErrorMessage")
      }
    }
  }


  implicit def rootEitherFormat[A : RootJsonFormat, B : RootJsonFormat] = new RootJsonFormat[Either[A, B]] {
    val format = DefaultJsonProtocol.eitherFormat[A, B]

    def write(either: Either[A, B]) = format.write(either)

    def read(value: JsValue) = format.read(value)
  }
}
