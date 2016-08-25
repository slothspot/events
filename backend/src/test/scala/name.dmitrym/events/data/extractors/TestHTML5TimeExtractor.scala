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

import org.specs2.mutable.Specification

import scala.concurrent.duration._

class TestHTML5TimeExtractor extends Specification {
  import HTML5TimeExtractor._
  "HTML5TimeExtractor" should {
    "parse P7D" in {
      parse("P7D") mustEqual FiniteDuration(7, DAYS)
    }
    "parse PT98M" in {
      parse("PT98M") mustEqual FiniteDuration(98, MINUTES)
    }
    "parse PT14H" in {
      parse("PT14H") mustEqual FiniteDuration(14, HOURS)
    }
    "parse PT18S" in {
      parse("PT18S") mustEqual FiniteDuration(18, SECONDS)
    }
  }
}
