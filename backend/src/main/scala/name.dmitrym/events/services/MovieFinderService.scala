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

import com.databricks.spark.corenlp.functions._
import com.mongodb.spark.MongoSpark
import name.dmitrym.events.data.{GenericMongoCodec, Movie, MovieCodec}
import name.dmitrym.events.utils.Configuration
import org.apache.spark.ml.feature.Word2Vec
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.mongodb.scala._

class MovieFinderService(val collectionName: String,
                         val codec: GenericMongoCodec[Movie])
    extends Service[Movie] {
  def insert(m: Movie): Unit =
    col
      .insertOne(m)
      .subscribe((c: Completed) => logger.debug(s"Insert result: $c"))

  // Spark related
  val spark = SparkSession
    .builder()
    .master("local[2]")
    .appName("movieFinder")
    .config("spark.mongodb.input.uri", Configuration.mongoConfig.uri)
    .config("spark.mongodb.input.database", Configuration.mongoConfig.db)
    .config("spark.mongodb.input.collection", collectionName)
    .config("spark.mongodb.output.uri", Configuration.mongoConfig.uri)
    .config("spark.mongodb.output.database", Configuration.mongoConfig.db)
    .config("spark.mongodb.output.collection", collectionName)
    .getOrCreate()

  import spark.implicits._

  def train(): Unit = {
    val moviesDF = MongoSpark.load(spark.sparkContext).toDF()
    val sentencesDF = moviesDF
      .select(explode(ssplit('storyline)).as('sentence))
      .select(tokenize('sentence).as('words))
      .cache()
    val word2vec = new Word2Vec()
      .setInputCol("words")
      .setOutputCol("result")
      .setVectorSize(7)
      .setMinCount(0)
    val model = word2vec.fit(sentencesDF)
  }
}

object MovieFinderService {
  def apply(): MovieFinderService =
    new MovieFinderService("movies", new MovieCodec)
}
