package api

import common.actors.LookupBusinessActor
import core.DefaultTimeout
import spray.routing.Directives

trait RestServiceBase extends DefaultTimeout with LookupBusinessActor with Directives
