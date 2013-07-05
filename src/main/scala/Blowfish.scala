package hu.gansperger.panda

import javax.crypto.spec.SecretKeySpec
import javax.crypto.Cipher
import net.liftweb.json._

object Blowfish {
  implicit val formats = net.liftweb.json.DefaultFormats
  private val CIPHER = "Blowfish/ECB/PKCS5Padding"

  private val blowfishEncrypter = Cipher.getInstance(CIPHER)
  blowfishEncrypter.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(Data.data("encrypt").getBytes("ASCII"), "Blowfish"))

  private val blowfishDecrypter = Cipher.getInstance(CIPHER)
  blowfishDecrypter.init(Cipher.DECRYPT_MODE, new SecretKeySpec(Data.data("decrypt").getBytes("ASCII"), "Blowfish"))

  def encrypt(src: JValue): String =
    blowfishEncrypter.doFinal(compact(render(src)).getBytes("utf-8")).map("%02x" format _).mkString

  def decrypt(src:String): String =
    blowfishDecrypter.doFinal(hex2Bytes(src)).map(_.toChar).mkString

  private def hex2Bytes( hex: String ): Array[Byte] = {
    (for( elem <- hex.grouped(2))
      yield Integer.parseInt(elem,16).toByte).toArray
  }

  private def pad(s: String, l: Int):String =
    s + "\0" * (l - s.length)
}
