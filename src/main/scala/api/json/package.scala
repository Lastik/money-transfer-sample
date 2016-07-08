package api

import core.model.ModelEntityKey
import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

package object json {

  class ModelEntityKeyJsonFormat[T <: ModelEntityKey : Manifest] extends RootJsonFormat[T] {
    def write(obj: T): JsValue = JsString(obj.id.toString)

    def read(json: JsValue): T = json match {
      case JsString(str) =>
        manifest.runtimeClass.getConstructors()(0).newInstance(str).asInstanceOf[T]
      case _ => throw new DeserializationException("ModelEntityKeyJsonFormat:read method: JsString expected")
    }
  }

}
