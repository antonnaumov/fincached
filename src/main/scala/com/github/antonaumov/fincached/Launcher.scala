package com.github.antonaumov.fincached

import java.util.concurrent.TimeUnit

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{Controller, HttpServer}
import net.spy.memcached.MemcachedClient

/**
 * Created by antonnaumov on 7/21/15.
 */
object Launcher extends HttpServer {

  override protected def configureHttp(router: HttpRouter): Unit = {
    router.
      add[MemcachedController]
  }

  class MemcachedController
    extends Controller
    with MemcachedFacadeComponent
    with MustacheComponent
    with EnvironmentConfigurationComponent {
    get("/") { request: Request =>
      response.ok.view("index.mustache", indexViewModel(config.connectionString.split(",")))
    }

    get("/stat/:host?") { request: Request =>
      loanClient(request) { implicit client =>
        response.ok.view("stat.mustache", statisticViewModel(statistic(), request.params.getBooleanOrElse("raw", default = false)))
      }
    }

    get("/stat/hits/:host?") { request: Request =>
      loanClient(request) { implicit client =>
        response.ok.json(graphViewModel(statistic()))
      }
    }

    post("/flush/:host?") { request: Request =>
      loanClient(request) { implicit client =>
        response.ok.json(Map("result" -> flush().get(30, TimeUnit.SECONDS)))
      }
    }

    private def loanClient(request: Request)(f: MemcachedClient => ResponseBuilder#EnrichedResponse): ResponseBuilder#EnrichedResponse = {
      val client = clients.getOrElse(request.params("host"), {
        val newClient = createClient(
          request.params.get("host"),
          config.userName,
          config.password)
        clients(request.params("host")) = newClient
        newClient
      })
      f(client)
    }

    private val clients = scala.collection.mutable.Map[String, MemcachedClient]()
  }
}
