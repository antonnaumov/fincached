package com.github.antonaumov.fincached

import java.net.SocketAddress

import net.spy.memcached.auth.{AuthDescriptor, PlainCallbackHandler}
import net.spy.memcached.internal.OperationFuture
import net.spy.memcached.{AddrUtil, ConnectionFactoryBuilder, FailureMode, MemcachedClient}

/**
 * Created by antonnaumov on 7/21/15.
 */
trait MemcachedFacadeComponent {
  def createClient(connectionString: Option[String] = None,
                   userName: Option[String] = None,
                   password: Option[String] = None): MemcachedClient = {
    val connectionFactory = {
      val builder = new ConnectionFactoryBuilder()
        .setProtocol(ConnectionFactoryBuilder.Protocol.BINARY)
        .setMaxReconnectDelay(20)
        .setFailureMode(FailureMode.Cancel)
      (userName, password) match {
        case (Some(u), Some(pwd)) =>
          builder.setAuthDescriptor(new AuthDescriptor(Array("PLAIN"), new PlainCallbackHandler(u, pwd)))
        case _ =>
      }
      builder.build()
    }
    new MemcachedClient(connectionFactory, AddrUtil.getAddresses(connectionString.getOrElse("localhost:11211")))
  }

  def flush()(implicit client: MemcachedClient): OperationFuture[java.lang.Boolean] = {
    client.flush()
  }

  def statistic()(implicit client: MemcachedClient): Map[SocketAddress, Map[String, String]] = {
    import collection.JavaConverters._
    client.getStats.asScala.toMap.mapValues(_.asScala.toMap)
  }
}
