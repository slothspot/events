// Copyright (C) 2016 Dmitry Melnichenko.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package name.dmitrym.events.utils

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

case class NetworkConfig(interface: String, port: Int)
case class MongoConfig(uri: String, db: String)

object Configuration extends LazyLogging {
  private[this] val cfg = {
    val clsLoader = Configuration.getClass.getClassLoader

    if (ConfigFactory.parseResources(clsLoader, "application-dev.conf") == ConfigFactory
          .empty()) {
      logger.debug("Loading production configuration")
      ConfigFactory.load(clsLoader, "application.conf")
    } else {
      logger.debug("Loading test configuration")
      ConfigFactory.load(clsLoader, "application-dev.conf")
    }
  }

  lazy val networkConfig: NetworkConfig = {
    val interface = cfg.getString("service.network.interface")
    val port = cfg.getInt("service.network.port")
    NetworkConfig(interface, port)
  }

  lazy val mongoConfig: MongoConfig = {
    val uri = cfg.getString("service.mongo.uri")
    val db = cfg.getString("service.mongo.db")
    MongoConfig(uri, db)
  }
}
