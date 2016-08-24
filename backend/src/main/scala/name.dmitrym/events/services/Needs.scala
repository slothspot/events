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
    def readOptionDate() = Some(new Date(reader.readDateTime()))

    var title = ""
    var completed = false
    var createdDate: Date = null // scalastyle:ignore
    var updatedDate: Option[Date] = None
    var completedDate: Option[Date] = None

    reader.readStartDocument()
    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      reader.readName() match {
        case "title" => title = reader.readString()
        case "completed" => completed = reader.readBoolean()
        case "createdDate" => createdDate = new Date(reader.readDateTime())
        case "updatedDate" => updatedDate = readOptionDate()
        case "completedDate" => completedDate = readOptionDate()
        case "_id" => reader.readObjectId()
        case n => logger.error(s"Unsupported field name $n")
      }
    }
    reader.readEndDocument()

    Need(title, completed, createdDate, updatedDate, completedDate)
  }

  override def encode(writer: BsonWriter,
                      value: Need,
                      encoderContext: EncoderContext): Unit = {
    def writeOptionDate(n: String, d: Option[Date]): Unit = {
      d match {
        case Some(v) => writer.writeDateTime(n, v.getTime)
        case None => () // do nothing if field is not set
      }
    }
    writer.writeStartDocument()
    writer.writeString("title", value.title)
    writer.writeBoolean("completed", value.completed)
    writer.writeDateTime("createdDate", value.createdDate.getTime)
    writeOptionDate("updatedDate", value.updatedDate)
    writeOptionDate("completedDate", value.completedDate)
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
