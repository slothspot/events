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

import name.dmitrym.events.data.{GenericMongoCodec, Need, NeedCodec}
import org.mongodb.scala.{Completed, Observable}

import scala.reflect.ClassTag

class NeedsService[T](val collectionName: String,
                      val codec: GenericMongoCodec[T])
    extends Service[T] {
  def create(n: T)(implicit ct: ClassTag[T]): Observable[Completed] =
    col.insertOne(n)
}

object NeedsService {
  def apply(): NeedsService[Need] = new NeedsService("needs", new NeedCodec)
}
