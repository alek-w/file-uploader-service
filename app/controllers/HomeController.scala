package controllers

import java.io.File
import java.nio.file.StandardCopyOption._
import java.nio.file.attribute.PosixFilePermission._
import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.{Files, Path, Paths}
import java.util
import javax.inject._

import akka.stream.IOResult
import akka.stream.scaladsl._
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi
import play.api.libs.streams._
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.core.parsers.Multipart.FileInfo
import producer.AsyncProducer

import scala.concurrent.Future

case class FormData(name: String)

@Singleton
class HomeController @Inject()(implicit val messagesApi: MessagesApi) extends Controller with i18n.I18nSupport {

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)
  private val config = ConfigFactory.load()

  private val storageFolder = config.getString("storageFolder")
  private val topic = config.getString("kafka.topic")

  private val producerHost =  config.getString("kafka.producer.host")
  private val producerPort = config.getString("kafka.producer.port")

  val form = Form(
    mapping(
      "file" -> text
    )(FormData.apply)(FormData.unapply)
  )

  def index = Action { implicit request =>
    Ok(views.html.index(form))
  }

  type FilePartHandler[A] = FileInfo => Accumulator[ByteString, FilePart[A]]

  private def handleFilePartAsFile: FilePartHandler[File] = {
    case FileInfo(partName, filename, contentType) =>
      val attr = PosixFilePermissions.asFileAttribute(util.EnumSet.of(OWNER_READ, OWNER_WRITE))
      val path: Path = Files.createTempFile("multipartBody", "tempFile", attr)
      val file = path.toFile
      val fileSink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(file.toPath)
      val accumulator: Accumulator[ByteString, IOResult] = Accumulator(fileSink)
      accumulator.map {
        case IOResult(count, status) =>
          FilePart(partName, filename, contentType, file)
      }(play.api.libs.concurrent.Execution.defaultContext)
  }


  private def copyFile(file: File, name: String) = {
    Files.copy(file.toPath, Paths.get(storageFolder, name), REPLACE_EXISTING)
  }

  private def deleteTempFile(file: File) = Files.deleteIfExists(file.toPath)

  private def operateOnTempFile(file: File) = {
    val size = Files.size(file.toPath)
    logger.info("size = " + size)
    Files.deleteIfExists(file.toPath)
    size
  }

  /**
    *
    * Uploads a file as a POST request.
    * Endpoint:
    * http://localhost:9000/upload
    *
    *
    * url example of getting the file :
    * http://127.0.0.1:9000/assets/images/favicon.png
    *
    * @return
    */
  def upload: Action[MultipartFormData[File]] = Action(parse.multipartFormData(handleFilePartAsFile)) { implicit request =>
    val fileOption = request.body.file("name").map {
      case FilePart(key, filename, contentType, file) =>

        val timestamp: Long = System.currentTimeMillis

        val newFileName = timestamp + filename
        val copy = copyFile(file, newFileName)
        val deleted = deleteTempFile(file)

        notify(newFileName)

        copy
    }
    Ok(s"Uploaded: $fileOption")
  }

  private val producer = new AsyncProducer(producerHost + ":" + producerPort)

  private def notify(filename: String) = {
    producer.send(topic, filename)
    logger.debug("message sent:" + filename)
  }

}
