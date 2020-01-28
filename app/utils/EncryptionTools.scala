package utils

import java.util.UUID

@Singleton
class EncryptionTools {

  def uuid: String = UUID.randomUUID().toString
}
