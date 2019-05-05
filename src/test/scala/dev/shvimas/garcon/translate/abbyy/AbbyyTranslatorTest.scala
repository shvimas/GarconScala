package dev.shvimas.garcon.translate.abbyy

import dev.shvimas.garcon.translate.{LanguageCode, Translation}
import org.scalatest.FunSuite

import scala.util._

class AbbyyTranslatorTest extends FunSuite {
  test("abbyy translator") {
    val translator: AbbyyTranslator = AbbyyTranslator()
    val from = LanguageCode.EN
    val to = LanguageCode.RU

    assert(translator.newToken.isSuccess)
    translator.translate("cat", from, to) match {
      case Failure(exception) => throw exception
      case Success(translation: Translation) =>
        assert(translation.translatedText == "кот, кошка")
    }
  }
}
