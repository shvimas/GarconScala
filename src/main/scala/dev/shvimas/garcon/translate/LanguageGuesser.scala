package dev.shvimas.garcon.translate

import dev.shvimas.garcon.translate.LanguageCode.LanguageCode

object LanguageGuesser {
  def testLanguage(languageCode: LanguageCode, text: String): Boolean = {
    languageCode match {
      case LanguageCode.EN =>
        text.forall(testUnicodeBlock(Character.UnicodeBlock.BASIC_LATIN))
      case LanguageCode.RU =>
        text.forall(testUnicodeBlock(Character.UnicodeBlock.CYRILLIC))
    }
  }

  private def testUnicodeBlock(block: Character.UnicodeBlock): Char => Boolean = {
    Character.UnicodeBlock.of(_).equals(block)
  }
}
