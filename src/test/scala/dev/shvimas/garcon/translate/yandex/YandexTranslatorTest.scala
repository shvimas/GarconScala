package dev.shvimas.garcon.translate.yandex

import com.typesafe.scalalogging.LazyLogging
import dev.shvimas.garcon.translate.{LanguageCode, Translator}
import org.scalatest.FunSuite

import scala.util.{Failure, Success}

class YandexTranslatorTest extends FunSuite with LazyLogging {

  test("translate") {
    val translator: Translator = new YandexTranslator
    val text = "apron"
    val src = LanguageCode.EN
    val dst = LanguageCode.RU
    translator.translate(text, src, dst) match {
      case Success(translation) =>
        assert(translation.originalText == text)
        assert(translation.translatedText == "фартук")
      case Failure(exception) =>
        fail(exception)
    }
  }

}
