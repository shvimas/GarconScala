package dev.shvimas.garcon.translate

import org.bson.{BsonReader, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}

//noinspection TypeAnnotation
object LanguageCode extends Enumeration {
  type LanguageCode = Value

  val EN = Value("en")
  val RU = Value("ru")

  object MongoCodec extends Codec[LanguageCode] {
    override def decode(reader: BsonReader,
                        decoderContext: DecoderContext): LanguageCode = {
      LanguageCode.withName(reader.readString())
    }

    override def encode(writer: BsonWriter,
                        value: LanguageCode,
                        encoderContext: EncoderContext): Unit = {
      writer.writeString(value.toString)
    }

    override def getEncoderClass: Class[LanguageCode] = classOf[LanguageCode]
  }

  object MongoCodecProvider extends CodecProvider {
    override def get[T](clazz: Class[T], registry: CodecRegistry): Codec[T] = {
      if (classOf[LanguageCode.Value].isAssignableFrom(clazz))
        MongoCodec.asInstanceOf[Codec[T]]
      else null
    }
  }
}
