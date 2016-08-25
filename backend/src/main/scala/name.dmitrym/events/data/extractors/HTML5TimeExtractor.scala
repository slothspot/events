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

package name.dmitrym.events.data.extractors

import scala.concurrent.duration._

/**
  * Converter from html5 duration format to Scala Duration
  * https://www.w3.org/TR/2014/REC-html5-20141028/infrastructure.html#durations
  * Notes:
  *   - Only simple cases like P7D, PT98M, PT14H, PT18S are handled.
  *   - Fraction of second is not supported
  *   - Duration time component is not supported
  * FIXME: implement proper duration parsing according to specification
  */
object HTML5TimeExtractor {
  def parse(input: String): Duration = {
    input(0) match {
      case 'P' =>
        input.last match {
          case 'D' =>
            FiniteDuration(input.substring(1, input.length - 1).toLong, DAYS)
          case u @ ('M' | 'H' | 'S') =>
            input(1) match {
              case 'T' =>
                val length = input.substring(2, input.length - 1).toLong
                val tu = u match {
                  case 'M' => MINUTES
                  case 'H' => HOURS
                  case 'S' => SECONDS
                }
                FiniteDuration(length, tu)
            }
        }
      case _ => throw new IllegalArgumentException("Not supported format")
    }
  }
}
