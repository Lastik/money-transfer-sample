package api.json

import common.{ErrorMessage, ServiceSuccess}
import spray.json.{DefaultJsonProtocol, JsBoolean, JsObject, JsString, JsValue, JsonFormat, RootJsonFormat}

trait CommonJsonProtocol {
  this: DefaultJsonProtocol =>

  implicit def serviceSuccessJsonFormat[A](implicit format: JsonFormat[A]) = new RootJsonFormat[ServiceSuccess[A]] {

    override def write(value: ServiceSuccess[A]): JsValue = {
      JsObject("ok" -> JsBoolean(true), "result" -> format.write(value.result))
    }

    override def read(json: JsValue): ServiceSuccess[A] = {
      throw new UnsupportedOperationException("Not supported")
    }
  }

  implicit object errorMessageJsonFormat extends RootJsonFormat[ErrorMessage] {

    override def write(value: ErrorMessage): JsValue = {
      JsObject("ok" -> JsBoolean(false), "error" -> JsString(value.text))
    }

    override def read(json: JsValue): ErrorMessage = {
      throw new UnsupportedOperationException("Not supported")
    }
  }

  implicit def rootEitherFormat[A : RootJsonFormat, B : RootJsonFormat] = new RootJsonFormat[Either[A, B]] {
    val format = DefaultJsonProtocol.eitherFormat[A, B]

    def write(either: Either[A, B]) = format.write(either)

    def read(value: JsValue) = format.read(value)
  }
}
