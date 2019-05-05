package dev.shvimas.garcon.translate

import dev.shvimas.garcon.translate.LanguageCode.LanguageCode

import scala.util.Try

trait Translator {
  type LanguageCodeImpl

  def translate(text: String,
                srcLangCode: LanguageCode,
                dstLangCode: LanguageCode): Try[Translation] =
    translateImpl(
      text = text,
      srcLang = toLanguageCodeImpl(srcLangCode),
      dstLang = toLanguageCodeImpl(dstLangCode)
    )

  def translateImpl(text: String,
                    srcLang: LanguageCodeImpl,
                    dstLang: LanguageCodeImpl): Try[Translation]

  def toLanguageCodeImpl(languageCode: LanguageCode): LanguageCodeImpl
}

trait Translation {
  val translatedText: String
  val originalText: String
  val translatorName: String
}


