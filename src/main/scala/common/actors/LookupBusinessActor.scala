package common.actors

import akka.actor.{ActorContext, ActorSystem}

trait LookupBusinessActor extends LookupActor{

  val basePath = "/user/"

}

trait LookupActor {

  def basePath: String

  def lookup(id: String)(implicit system: ActorSystem) = {
    system.actorSelection(basePath + id)
  }
  def lookupByContext(id: String)(implicit context: ActorContext) = {
    context.actorSelection(basePath + id)
  }

}

