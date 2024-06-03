//> using scala 3
//> using dep "dev.zio::zio::2.1.1"
//> using dep "dev.zio::zio-json::0.7.0"
//> using dep "dev.zio::zio-kafka::2.7.4"
//> using dep "dev.zio::zio-kafka-testkit::2.7.4"

import zio.*
import zio.json.*
import zio.kafka.consumer.*
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.*
import zio.stream.ZStream
import scala.util.Random as SRandom

case class Ramen(brand: String, flavor: Flavor, price: Double)
object Ramen:
  given JsonCodec[Ramen] = DeriveJsonCodec.gen[Ramen]
  val valueSerializer = Serde.string.inmapM[Any, Ramen](s =>
    ZIO
      .fromEither(s.fromJson[Ramen])
      .mapError(e => new RuntimeException(e))
  )(r => ZIO.succeed(r.toJson))

enum Flavor:
  case 🥬, 🐓, 🐄, 🦐, 🐖
object Flavor:
  given JsonCodec[Flavor] =
    JsonCodec[String].transform(Flavor.valueOf, _.toString)

object MainApp extends ZIOAppDefault:
  val producer =
    ZStream
      .fromZIO(Random.nextIntBetween(0, Int.MaxValue))
      .forever
      .mapZIO { id =>
        Producer.produce[Any, Int, Ramen](
          topic = "ramen",
          key = id,
          value = Ramen(
            SRandom.shuffle(List("Nissin", "Ichiran", "Samyang")).head,
            Flavor.fromOrdinal(id % 5),
            SRandom.between(0.0, 1.0)
          ),
          keySerializer = Serde.int,
          Ramen.valueSerializer
        )
      }
      .tap(r => Console.printLine(f"Sent value ${r}"))
      .drain

  val consumer =
    Consumer
      .plainStream(
        Subscription.topics("ramen"),
        Serde.int,
        Ramen.valueSerializer
      )
      .filter(_.value.price < 0.5)
      .tap(r => Console.printLine(f"Received ${r.value}"))
      .map(_.offset)
      .aggregateAsync(Consumer.offsetBatches)
      .mapZIO(_.commit)
      .drain

  def producerLayer =
    ZLayer.scoped(
      Producer.make(
        settings = ProducerSettings(List("localhost:9092"))
      )
    )

  def consumerLayer =
    ZLayer.scoped(
      Consumer.make(
        ConsumerSettings(List("localhost:9092")).withGroupId("ramen-slurper")
      )
    )

  override def run =
    producer
      .merge(consumer)
      .runDrain
      .provide(producerLayer, consumerLayer)
      .timeout(2.seconds)
      .exitCode
