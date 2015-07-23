package com.github.antonaumov.fincached

import com.typesafe.config.ConfigFactory

/**
 * Created by antonnaumov on 7/23/15.
 */
trait EnvironmentConfigurationComponent {
  val config: Config = new Config {
    override val connectionString: String = Option(typeSafeConfig).map(_.getString("MEMCACHED_URL")).getOrElse("localhost:11211")
    override val userName: Option[String] = Option(typeSafeConfig).map(_.getString("MEMCACHED_LOGIN"))
    override val password: Option[String] = Option(typeSafeConfig).map(_.getString("MEMCACHED_PASSWORD"))
  }

  private val typeSafeConfig = ConfigFactory.systemEnvironment() withFallback ConfigFactory.systemProperties()
}
