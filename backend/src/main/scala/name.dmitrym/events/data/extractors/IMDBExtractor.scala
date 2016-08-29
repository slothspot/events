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

import java.io.{File, FileInputStream, IOException, InputStream}

import scala.collection.JavaConversions._
import com.typesafe.scalalogging.LazyLogging
import name.dmitrym.events.data.Movie
import org.jsoup.Jsoup

object IMDBExtractor extends LazyLogging {
  def parse(f: File, charset: String = "UTF-8"): Option[Movie] =
    parse(new FileInputStream(f), charset, f.getAbsolutePath)
  def parse(io: InputStream, charset: String, baseUri: String): Option[Movie] = {
    try {
      val doc = Jsoup.parse(io, charset, baseUri)
      val briefMovieInfo = doc.select("div.title_wrapper").first
      val titleYearString = briefMovieInfo.select("h1[itemprop=name]").text
      val yearStartIdx = math.max(titleYearString.lastIndexOf(0x20),
                                  titleYearString.lastIndexOf(0xa0))
      val title = titleYearString.substring(0, yearStartIdx)
      val year = titleYearString
        .substring(yearStartIdx + 2, titleYearString.length - 1)
        .toInt
      val infoElement = briefMovieInfo.select("div.subtext")
      val rating =
        infoElement.select("meta[itemprop=contentRating]").attr("content")
      val duration = HTML5TimeExtractor.parse(
        infoElement.select("time[itemprop=duration]").attr("datetime"))
      val genres = infoElement.select("span[itemprop=genre]").map(_.text)
      val score =
        doc.select("div.ratings_wrapper span[itemprop=ratingValue]").text
      val plotSummary = doc.select("div.plot_summary")
      val briefDescription =
        plotSummary.select("div[itemprop=description]").text
      val director = plotSummary
        .select("span[itemprop=director] span[itemprop=name]")
        .map(_.text)
      val writers = plotSummary
        .select("span[itemprop=creator] span[itemprop=name]")
        .map(_.text)
      val stars = plotSummary
        .select("span[itemprop=actors] span[itemprop=name]")
        .map(_.text)
      val storyArticle = doc.select("div#titleStoryLine")
      val storyline = storyArticle.select("div[itemprop=description] p").text
      val keywords = storyArticle
        .select("div[itemprop=keywords] span[itemprop=keywords]")
        .map(_.text)
      val cast = doc
        .select("div#titleCast td[itemprop=actor] span[itemprop=name]")
        .map(_.text)
      Some(Movie(title, year, briefDescription, storyline))
    } catch {
      case e: IOException =>
        logger.error(s"Can't parse $baseUri", e)
        None
    }
  }
}
