package examples
import com.spotify.scio._
import com.spotify.scio.avro._

object TestModel {

  @AvroType.fromSchema(
    """{
      | "type":"record",
      | "name":"TestObject",
      | "namespace":"com.spotify.scio.avro",
      | "doc":"Record for an account",
      | "fields":[
      |   {"name":"field1","type":"int"},
      |   {"name":"field2","type":"int"}
      |  ]}
      """.stripMargin)
  class TestSchema

}