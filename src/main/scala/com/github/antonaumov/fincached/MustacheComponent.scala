package com.github.antonaumov.fincached

import java.net.{InetSocketAddress, SocketAddress}
import java.text.DecimalFormat

import com.twitter.finatra.response.Mustache

/**
 * Created by antonnaumov on 7/22/15.
 */
trait MustacheComponent {
  def indexViewModel(hosts: Array[String]) = IndexView(hosts.map(h => EndpointView(h.trim)))

  def statisticViewModel(stats: Map[SocketAddress, Map[String, String]], raw: Boolean) = {
    StatisticView(stats.head, raw)
  }

  def graphViewModel(stats: Map[SocketAddress, Map[String, String]]) = {
    HitCount(stats.head._2)
  }

  @Mustache("index")
  case class IndexView(hosts: Seq[EndpointView])
  case class EndpointView(id: String, name: String)
  object EndpointView {
    def apply(endpoint: String): EndpointView = {
      new EndpointView(endpoint.replaceAll(hostToIdRegex, "_"), endpoint)
    }
  }

  @Mustache("stat")
  case class StatisticView(id: String,
                           host: String,
                           status: String,
                           version: String,
                           memoryUsage: MemoryUsage,
                           itemsTotal: Long,
                           uptime: String,
                           connections: Int,
                           hitCount: HitCount,
                           stat: Seq[StatisticItem])
  object StatisticView {
    def apply(entry: (SocketAddress, Map[String, String]), rawStat: Boolean = false): StatisticView = {
      val host = socketAddressToString(entry._1)
      new StatisticView(
        id = host.replaceAll(hostToIdRegex, "_"),
        host = host,
        status = "open",
        version = entry._2.getOrElse("version", "unknown"),
        MemoryUsage(entry._2.get("bytes").map(_.toLong).getOrElse(0L), entry._2.get("limit_maxbytes").map(_.toLong).getOrElse(0L)),
        itemsTotal = entry._2.get("curr_items").map(_.toLong).getOrElse(0L),
        uptime = entry._2.get("uptime").map(_.toLong / 60 / 60).map(hours => s"${hours / 24} days ${hours % 24} hours").getOrElse("0 d 0 h"),
        connections = entry._2.get("curr_connections").map(_.toInt).getOrElse(0),
        hitCount = HitCount(entry._2),
        stat = if (rawStat) entry._2.map(StatisticItem(_)).toSeq else Seq()
      )
    }

    private def socketAddressToString(address: SocketAddress): String = {
      address match {
        case ia: InetSocketAddress =>
          s"${ia.getHostName}:${ia.getPort}"
        case _ =>
          address.toString
      }
    }
  }
  case class StatisticItem(key: String, value: String)
  object StatisticItem {
    def apply(entry: (String, String)): StatisticItem =
      new StatisticItem(entry._1, entry._2)
  }

  case class HitCount(get: Long,
                      set: Long,
                      others: Long,
                      total: Long)
  object HitCount {
    def apply(stat: Map[String, String]): HitCount = {
      val get = stat.get("cmd_get").map(_.toLong).getOrElse(0L)
      val set = stat.get("cmd_set").map(_.toLong).getOrElse(0L)
      val others = stat.get("cmd_flush").map(_.toLong).getOrElse(0L) + stat.get("cmd_touch").map(_.toLong).getOrElse(0L)
      val total = get + set + others
      new HitCount(
        get / 1000 / 1000,
        set / 1000 / 1000,
        others / 1000 / 1000,
        total / 1000 / 1000
      )
    }
  }
  case class MemoryUsage(mbUsed: String,
                         mbTotal: String,
                         green: Int,
                         yellow: Option[Int] = None,
                         red: Option[Int] = None)
  object MemoryUsage {
    def apply(bytesUsed: Long, bytesTotal: Long): MemoryUsage = {
      val ratioInPercent = (bytesUsed * 100 / avoidDivideByZero(bytesTotal, 100L)).toInt
      new MemoryUsage(
        doublePrintFormat.format(bytesUsed / 1024.0D / 1024.0D),
        doublePrintFormat.format(bytesTotal / 1024.0D / 1024.0D),
        if (ratioInPercent < 60) ratioInPercent else 60,
        if (ratioInPercent > 60 && ratioInPercent < 85) Some(ratioInPercent - 60) else if (ratioInPercent > 60) Some(25) else None,
        if (ratioInPercent > 85 && ratioInPercent < 100) Some(100 - ratioInPercent) else if (ratioInPercent >= 100) Some(100) else None
      )
    }
  }

  private def avoidDivideByZero[T <: AnyVal](value: T, nonZero: T): T = if (value == 0) nonZero else value

  private val doublePrintFormat = new DecimalFormat("#0.00")
  private val hostToIdRegex = "[.|:]"
}
