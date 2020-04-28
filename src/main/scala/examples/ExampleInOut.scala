package examples

import com.spotify.scio._
import com.spotify.scio.avro.types.AvroType
import com.spotify.scio.coders.Coder
import com.spotify.scio.io.ClosedTap
import com.spotify.scio.values.SCollection
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.beam.sdk.coders.StringUtf8Coder
import org.apache.beam.sdk.io.FileIO
import org.apache.beam.sdk.io.FileIO.Write.FileNaming
import org.apache.beam.sdk.io.parquet.ParquetIO
import org.apache.beam.sdk.transforms.{Contextful, SerializableFunction}

import scala.collection.JavaConverters._

object ExampleInOut {

  @AvroType.fromSchema("""{
                         | "type":"record",
                         | "name":"Account",
                         | "namespace":"com.spotify.scio.avro",
                         | "doc":"Record for an account",
                         | "fields":[
                         |   {"name":"id","type":"int"},
                         |   {"name":"type","type":"string"},
                         |   {"name":"name","type":"string"},
                         |   {"name":"amount","type":"double"}]}
    """.stripMargin)
  class AccountFromSchema

  @AvroType.toSchema
  case class AccountToSchema(id: Int, `type`: String, name: String, amount: Double)

  def main(cmdlineArgs: Array[String]): Unit = {
    // Create `ScioContext` and `Args`
    val (sc, args) = ContextAndArgs(cmdlineArgs)


    val dynamicOutput: FileIO.Write[String, GenericRecord] = FileIO
      .writeDynamic[String, GenericRecord]()
      .by((input: GenericRecord) => {
        input.get("id").toString.toUpperCase  + "/"
      })
      .withDestinationCoder(StringUtf8Coder.of())
      .withNumShards(1) // Since input is small, restrict to one file per bucket
      .withNaming(
        new SerializableFunction[String, FileNaming] {
          override def apply(partitionCol: String): FileNaming = {
            FileIO.Write.defaultNaming(s"Id=$partitionCol", ".parquet")
          }
        }
      )
      .via(Contextful.fn[GenericRecord,GenericRecord](
          new SerializableFunction[GenericRecord,GenericRecord]{
            override def apply(input: GenericRecord): GenericRecord = {
              val r = new GenericData.Record(outputSchema)
              r.put("amount",input.get("amount"))
              r.put("name",input.get("name"))
              r.put("type",input.get("type"))
              r
            }
          }
        ),
        ParquetIO.sink(outputSchema)
      )
      .to("gs://bucket-name/table-name")

    val m: SCollection[GenericRecord] = genericOut(sc,args,dynamicOutput)

    m.saveAsCustomOutput("dynamicWriteExample", dynamicOutput)
    sc.run()
    ()
  }
  private def genericOut(sc: ScioContext, args: Args,dynamicOutput:FileIO.Write[String,GenericRecord]): SCollection[GenericRecord] = {
    // Avro generic record encoding is more efficient with an explicit schema
    implicit def genericCoder = Coder.avroGenericRecordCoder(schema)
    sc.parallelize(1 to 100)
      .map[GenericRecord] { i =>
        val r = new GenericData.Record(schema)
        r.put("id", i)
        r.put("amount", i.toDouble)
        r.put("name", "account_" + i)
        r.put("type", "checking")
        r
      }
  }
  val schema = {
    def f(name: String, tpe: Schema.Type) =
      new Schema.Field(
        name,
        Schema.createUnion(List(Schema.create(Schema.Type.NULL), Schema.create(tpe)).asJava),
        null: String,
        null: AnyRef
      )

    val s = Schema.createRecord("GenericAccountRecord", null, null, false)
    s.setFields(
      List(
        f("id", Schema.Type.INT),
        f("amount", Schema.Type.DOUBLE),
        f("name", Schema.Type.STRING),
        f("type", Schema.Type.STRING)
      ).asJava
    )
    s
  }

  val outputSchema = {
    def f(name: String, tpe: Schema.Type) =
      new Schema.Field(
        name,
        Schema.createUnion(List(Schema.create(Schema.Type.NULL), Schema.create(tpe)).asJava),
        null: String,
        null: AnyRef
      )

    val s = Schema.createRecord("GenericPartAccountRecord", null, null, false)
    s.setFields(
      List(
        f("amount", Schema.Type.DOUBLE),
        f("name", Schema.Type.STRING),
        f("type", Schema.Type.STRING)
      ).asJava
    )
    s
  }
}
