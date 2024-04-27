//> using scala 3
//> using dep org.apache.spark:spark-sql-api_2.13:3.5.1
//> using dep io.github.vincenzobaz::spark-scala3-encoders:0.2.6

import org.apache.spark.sql.SparkSession
import scala3encoders.given

case class Person(
    name: String,
    age: Int
)

@main def main =
  val spark = SparkSession
    .builder()
    .config("spark.log.level", "WARN")
    .getOrCreate()

  import spark.implicits.*

  val people = Seq(
    Person("Suzuki", 16),
    Person("Suzuki", 41),
    Person("Mishima", 25),
    Person("Marukami", 65),
    Person("Marukami", 49),
    Person("Marukami", 75)
  )

  people.toDS
    .groupByKey(_.name)
    .mapGroups { (name, peopleWithSameName) =>
      val ages = peopleWithSameName.map(_.age).toList
      (
        name,
        ages.min,
        ages.max,
        ages.sum.toDouble / ages.length
      )
    }
    .map(statistics =>
      (statistics._1, statistics._2, statistics._3, statistics._4)
    )
    .show()

  people.toDS.createOrReplaceTempView("people")

  val sqlDF =
    spark.sql(
      """
      SELECT name, COUNT(*) AS cnt 
      FROM people 
      GROUP BY name
      """
    )
  sqlDF.show()
