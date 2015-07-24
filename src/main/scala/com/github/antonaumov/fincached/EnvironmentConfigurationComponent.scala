package com.github.antonaumov.fincached

/**
 * Created by antonnaumov on 7/23/15.
 */
trait EnvironmentConfigurationComponent {
  val config: Config = new Config {
    override val connectionString: String = readEnvironmentProperty("MEMCACHED_URL").getOrElse("localhost:11211")
    override val userName: Option[String] = readEnvironmentProperty("MEMCACHED_LOGIN")
    override val password: Option[String] = readEnvironmentProperty("MEMCACHED_PASSWORD")
  }

  private def readEnvironmentProperty(name: String): Option[String] = {
    Option(System.getenv(name)) orElse {
      Option(System.getProperty(name))
    }
  }
}
