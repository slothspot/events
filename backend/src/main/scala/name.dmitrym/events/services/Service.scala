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

package name.dmitrym.events.services

import com.mongodb.async.client.MongoClients
import com.typesafe.scalalogging.LazyLogging
import name.dmitrym.events.data.GenericMongoCodec
import name.dmitrym.events.storage.MongoConnection
import org.bson.codecs.configuration.CodecRegistries

import scala.reflect.ClassTag

trait Service[T] extends LazyLogging {
  val collectionName: String
  val codec: GenericMongoCodec[T]

  private[this] val cr = CodecRegistries.fromRegistries(
    CodecRegistries.fromCodecs(codec),
    MongoClients.getDefaultCodecRegistry)
  protected[this] def col(implicit ct: ClassTag[T]) =
    MongoConnection.db.withCodecRegistry(cr).getCollection[T](collectionName)
}
