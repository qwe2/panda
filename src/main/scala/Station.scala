package hu.gansperger.panda

import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import java.io.{FileOutputStream, OutputStream, File}
import android.os.Environment
import scalaj.http.{HttpOptions, Http}
import scala.reflect.io.Streamable

class Station(client: Pandora, src: JValue) {
  implicit val formats = DefaultFormats

  val stationId = (src \\"stationId").extract[String]
  val stationToken= (src \\ "stationToken").extract[String]
  val isShared = (src \\ "isShared").extract[Boolean]
  val isQuickMix = (src \\ "isQuickMix").extract[Boolean]
  val stationName = (src \\ "stationName").extract[String]

  def getPlaylist(): Unit = {
    val playListData: JValue=
      ("stationToken" -> stationId)

    val response = client.sendJsonData("station.getPlaylist",playListData,https=true)

    val fst = (for{
      JArray(songs) <- (response \\ "items")
      JObject(song) <- songs
      if song.exists( elem => elem match{
        case JField("songName",_) => true
        case _ => false
      })
    }yield new Song(client,JObject(song),lowQuality)).head

    val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
    path.mkdir

    val file = new File(path, "asd.mp3")

    val out = new FileOutputStream(file)

    Http(fst.getSongUrl)
      .option(HttpOptions.connTimeout(1000))
      .option(HttpOptions.readTimeout(10000))
      .proxy("localhost",8118)
      {
        inputStream => {
          out.write(Streamable.bytes(inputStream))
          out.close
        }
    }
  }
}
