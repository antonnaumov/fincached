package com.github.antonaumov.fincached

/**
 * Created by antonnaumov on 7/23/15.
 */
trait Config {
  val connectionString: String
  val userName: Option[String]
  val password: Option[String]
}
