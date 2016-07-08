package api

import akka.actor.Props
import core.{Core, CoreActors}
import spray.routing.{Directives, RouteConcatenation}

/**
 * The REST API layer. It exposes the REST services, but does not provide any
 * web server interface.<br/>
 * Notice that it requires to be mixed in with ``core.CoreActors``, which provides access
 * to the top-level actors that make up the system.
 */
trait Api extends RouteConcatenation with Directives {
  this: CoreActors with Core =>


  private implicit val ec = system.dispatcher


  val routes =
    new CustomerRestService().route ~
      new AccountRestService().route

  val rootService = system.actorOf(Props(new RoutedHttpService(routes)))

}
