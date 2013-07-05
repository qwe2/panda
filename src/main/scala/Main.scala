package hu.gansperger.panda

import _root_.android.app.Activity
import _root_.android.os.Bundle
import _root_.android.util.Log

import java.net.URLEncoder
import java.io.InputStreamReader

import scala.util.parsing.json._
import scalaj.http._

import scala.concurrent._
import ExecutionContext.Implicits.global

import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST.JValue
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.HttpHost

class Main extends Activity with TypedActivity{

  val ENCODING = "utf-8"
  val PROTOCOL = "https://"

  /*val URLData = Map(
    "method" -> ""
  )

  val JSONData = JSONObject(Map(
    "username" -> Data.data("username"),
    "password" -> Data.data("password"),
    "deviceModel" -> Data.data("deviceId"),
    "version" -> Data.data("version")
  ))

  val receive = new Thread(new Runnable(){
    def run(){
      val url = makeURL(URLData.updated("method","auth.partnerLogin"),Data.data("url"),PROTOCOL)

      val response = Http.postData(url, JSONData.toString())
        .header("User-Agent","android")
        .header("Content-Type", "application/json")
        .header("Charset", "UTF-8")
        .option(HttpOptions.connTimeout(1000))
        .option(HttpOptions.readTimeout(5000))
        {inputStream =>
          JsonParser.parse(new InputStreamReader(inputStream))
        }

      Log.d("response",response.toString)

      runOnUiThread(new Runnable(){
        def run() {
          findView(TR.textview).setText(response.toString)
        }
      })
    }
  })*/

  val requestRunner = new Thread(new Runnable(){
    def run() = {
      val client = (new Pandora()).connect().login("xxx","xxx")
      client.getStations()
    }
  })

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)

    //Log.d("LOG",makeURL(Map("method" -> "auth.partnerLogin", "auth_token" -> "", "partner_id" -> "", "user_id" -> ""	),Data.data("url"),PROTOCOL))

    requestRunner.start

  }
}
