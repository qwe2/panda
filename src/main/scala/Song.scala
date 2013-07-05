package hu.gansperger.panda

import net.liftweb.json._

case class SongQuality(quality: String)
object highQuality extends SongQuality("highQuality")
object mediumQuality extends SongQuality("mediumQuality")
object lowQuality extends SongQuality("lowQuality")

class Song(client: Pandora, src: JValue, quality: SongQuality) {
  implicit val formats = DefaultFormats

  val albumName = (src \\ "albumName").extract[String]
  val artistName = (src \\ "artistName").extract[String]
  val songName = (src \\ "songName").extract[String]
  val audioUrlMap = (src \\ "audioUrlMap")
  /*self.trackToken = d['trackToken']
  self.rating = RATE_LOVE if d['songRating'] == 1 else RATE_NONE # banned songs won't play, so we don't care about them
  self.stationId = d['stationId']
  self.songDetailURL = d['songDetailUrl']
  self.artRadio = d['albumArtUrl']

  self.tired=False
  self.message=''
  self.start_time = None
  self.finished = False
  self.playlist_time = time.time()
  self.feedbackId = None*/

  def getSongUrl() : String = (audioUrlMap \ quality.quality \ "audioUrl").extract[String]

}
