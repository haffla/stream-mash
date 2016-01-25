package models.service.library

import java.io.File

import models.auth.Helper
import models.util.Constants
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import play.api.libs.Files
import play.api.mvc.MultipartFormData

object AudioFileLibrary {
  def apply(identifier: Either[Int, String]) = new AudioFileLibrary(identifier)
}

class AudioFileLibrary(identifier: Either[Int, String]) extends Library(identifier, "audio") {

  val audioPath = "/tmp/audiofiles/"
  new File(audioPath).mkdir()

  def handleFiles(files:Seq[MultipartFormData.FilePart[Files.TemporaryFile]]):Seq[Map[String,String]] = {
    val userId = Helper.userIdentifierToString(identifier)
    val userpath = audioPath + userId
    new File(userpath).mkdir()
    files map { file =>
      val filename = file.filename
      val filepath = userpath + "/" + filename
      file.ref.moveTo(new File(filepath))
      val audiofile = AudioFileIO.read(new File(filepath))
      val tag = audiofile.getTag
      Map(
        Constants.mapKeyArtist -> tag.getFirst(FieldKey.ARTIST),
        Constants.mapKeyAlbum -> tag.getFirst(FieldKey.ALBUM),
        Constants.mapKeyTrack -> tag.getFirst(FieldKey.TRACK)
      )
    }
  }

  def process(files:Seq[MultipartFormData.FilePart[Files.TemporaryFile]]):Unit = {
    convertSeqToMap(handleFiles(files))
    apiHelper.setRetrievalProcessDone()
  }
}
