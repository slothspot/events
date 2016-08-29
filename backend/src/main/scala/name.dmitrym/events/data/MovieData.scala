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

package name.dmitrym.events.data

import org.bson.{BsonReader, BsonType, BsonWriter}
import org.bson.codecs.{DecoderContext, EncoderContext}

case class Movie(title: String, year: Int, brief: String, storyline: String)

private[events] class MovieCodec extends GenericMongoCodec[Movie] {
  override def getEncoderClass: Class[Movie] = classOf[Movie]
  override def decode(reader: BsonReader,
                      decoderContext: DecoderContext): Movie = {
    var title = ""
    var year = 0
    var brief = ""
    var storyline = ""
    reader.readStartDocument()
    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      reader.readName() match {
        case "title" => title = reader.readString()
        case "year" => year = reader.readInt32()
        case "brief" => brief = reader.readString()
        case "storyline" => storyline = reader.readString()
      }
    }
    reader.readEndDocument()
    Movie(title, year, brief, storyline)
  }
  override def encode(writer: BsonWriter,
                      value: Movie,
                      encoderContext: EncoderContext): Unit = {
    writer.writeStartDocument()
    writer.writeString("title", value.title)
    writer.writeInt32("year", value.year)
    writer.writeString("brief", value.brief)
    writer.writeString("storyline", value.storyline)
    writer.writeEndDocument()
  }
}
