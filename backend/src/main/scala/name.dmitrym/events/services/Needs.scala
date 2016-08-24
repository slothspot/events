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

import java.util.Date

import com.mongodb.async.client.MongoClients
import com.typesafe.scalalogging.LazyLogging
import name.dmitrym.events.storage.MongoConnection
import org.bson.{BsonReader, BsonType, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.codecs.configuration.CodecRegistries

case class Need(title: String,
                completed: Boolean = false,
                createdDate: Date = new Date(),
                updatedDate: Option[Date] = None,
                completedDate: Option[Date] = None) {
  if (completed) {
    assert(completedDate.isDefined, "completedDate should be defined")
  } else {
    assert(completedDate.isEmpty, "completedDate should not be defined")
  }
  if (updatedDate.isDefined) {
    assert(createdDate.getTime <= updatedDate.get.getTime,
           "Update should not happen before create")

    if (completedDate.isDefined) {
      assert(updatedDate.get.getTime <= completedDate.get.getTime,
             "Update should not happen after complete")
    }
  }

  if (completedDate.isDefined) {
    assert(createdDate.getTime <= completedDate.get.getTime,
           "Complete should not happen before create")
  }
}

private[services] class NeedCodec extends Codec[Need] with LazyLogging {
  override def decode(reader: BsonReader,
                      decoderContext: DecoderContext): Need = {
    var title = ""
    var completed = false
    var createdDate = 0L
    var updatedDate = 0L
    var completedDate = 0L

    reader.readStartDocument()
    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      reader.readName() match {
        case "title" => title = reader.readString()
        case "completed" => completed = reader.readBoolean()
        case "createdDate" => createdDate = reader.readDateTime()
        case "updatedDate" => updatedDate = reader.readDateTime()
        case "completedDate" => completedDate = reader.readDateTime()
        case "_id" => reader.readObjectId()
        case n => logger.error(s"Unsupported field name $n")
      }
    }
    reader.readEndDocument()

    Need(title,
         completed,
         createdDate = new Date(createdDate),
         updatedDate = updatedDate match {
           case 0L => None
           case v => Some(new Date(v))
         },
         completedDate = completedDate match {
           case 0L => None
           case v => Some(new Date(v))
         })
  }

  override def encode(writer: BsonWriter,
                      value: Need,
                      encoderContext: EncoderContext): Unit = {
    writer.writeStartDocument()
    writer.writeString("title", value.title)
    writer.writeBoolean("completed", value.completed)
    writer.writeDateTime("createdDate", value.createdDate.getTime)
    value.updatedDate match {
      case Some(u) => writer.writeDateTime("updatedDate", u.getTime)
      case None => () // do nothing if field is not set
    }
    value.completedDate match {
      case Some(c) => writer.writeDateTime("completedDate", c.getTime)
      case None => () // do nothing if field is not set
    }
    writer.writeEndDocument()
  }

  override def getEncoderClass: Class[Need] = classOf[Need]
}

object Needs extends LazyLogging {
  private[this] val cr = CodecRegistries.fromRegistries(
    CodecRegistries.fromCodecs(new NeedCodec),
    MongoClients.getDefaultCodecRegistry)
  private[this] val col =
    MongoConnection.db.withCodecRegistry(cr).getCollection[Need]("needs")
}
