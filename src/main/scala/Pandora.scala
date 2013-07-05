package hu.gansperger.panda

import com.github.nscala_time.time.Imports._
import _root_.android.util.Log
import net.liftweb.json._
import scalaj.http._
import java.net.URLEncoder
import scala.io.Source
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JValue

/**
 * This class is responsible for handling the connection between the palyer and the Pandora API.
 * The connect() and login() functions are only for initializing the connection.
 *
 * @param urlArgs - The arguments that will be passed to the default Pandora query URL
 *
 */
//timeOffset: Option[DateTime], userAuthToken: Option[String], partnerAuthToken: Option[String]
class Pandora(urlArgs: Map[String,String] = Data.urlArgs, defaultArgs: JValue = new JObject(Nil), timeOffset:Option[Long] = None) {
  implicit val formats = net.liftweb.json.DefaultFormats
  private val ENCODING = "utf-8"

  private def mkUrl(args: Map[String,String], url:String, protocol:String):String =
    protocol + url + args.map(pair => pair._1 + "=" + URLEncoder.encode(pair._2,ENCODING)).mkString("&")

  def sendJsonData(method: String, data:JValue = new JObject(Nil), blowfish: Boolean = true, https: Boolean = false): JValue = {
    val url =
      if (https) mkUrl(urlArgs.updated("method",method),Data.data("url"),"https://")
      else mkUrl(urlArgs.updated("method",method),Data.data("url"),"http://")

    //val compData = defaultArgs merge data
    val compData = timeOffset match{
      case None => defaultArgs merge data
      case Some(x) =>
        val syncTime: JValue = ("syncTime" -> (DateTime.now.getMillis / 1000 + x).toInt)
        defaultArgs merge data merge syncTime
    }

    Log.d("PANDADBG",compact(render(compData)))

    val dataToSend =
      if(blowfish) Blowfish.encrypt(compData)
      else compact(render(compData))

    Http.postData(url,dataToSend)
      .header("User-Agent","android")
      .header("Content-Type", "text/plain")
      .header("Charset", "UTF-8")
      .option(HttpOptions.connTimeout(1000))
      .option(HttpOptions.readTimeout(5000))
      .proxy("localhost",8118)
    {inputStream =>
      val response = JsonParser.parse(Source.fromInputStream(inputStream).mkString)

      if ((response \ "stat") == JString("ok")) (response \ "result")
      else if ((response \ "stat") == JString("fail")){
        val msg = (response \ "message").extract[String]
        val code = (response \ "code").extract[Int]
        throw new Exception(msg+"; error code: "+code.toString)
      }
      else throw new Exception("Unknown error.")


      /*if(blowfish) JsonParser.parse(Blowfish.decrypt(response))
      else JsonParser.parse(response)*/
    }
  }

  def connect(): Pandora = {
    val connectData =
      ("username" -> Data.data("username")) ~
      ("password" -> Data.data("password")) ~
      ("deviceModel" -> Data.data("deviceId")) ~
      ("version" -> Data.data("version"))

    val response = sendJsonData("auth.partnerLogin",connectData,blowfish=false,https=true)

    val authToken = (response \\ "partnerAuthToken").extract[String]
    val partnerId = (response \\ "partnerId").extract[String]
    val syncTime = Integer.parseInt(Blowfish.decrypt((response \\ "syncTime").extract[String]).substring(4,14))
    val _timeOffset = syncTime - (DateTime.now.getMillis / 1000)

    new Pandora(urlArgs.updated("auth_token",authToken).updated("partner_id",partnerId),
      ("partnerAuthToken" -> authToken), Option(_timeOffset))

  }

  def login(username:String, password: String): Pandora = {
    val loginData =
      ("loginType" -> "user") ~
      ("username" -> username) ~
      ("password"-> password)

    val response = sendJsonData("auth.userLogin",loginData,https=true)

    val authToken = (response \\ "userAuthToken").extract[String]
    val userId = (response \\ "userId").extract[String]

    new Pandora(urlArgs.updated("auth_token",authToken).updated("user_id",userId),
      ("userAuthToken" -> authToken), timeOffset)
  }

  def getStations(): Unit = {
    val response = sendJsonData("user.getStationList")

    (for{
      JArray(items) <- (response \\ "stations")
      JObject(item) <- items
    }
    yield new Station(this,JObject(item))).tail.head.getPlaylist
  }
}

//class PandoraException extends Exception

