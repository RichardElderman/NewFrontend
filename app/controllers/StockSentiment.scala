package controllers

import javax.inject._

import play.api.Configuration
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import reactivemongo.api.Cursor
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo._
import reactivemongo.api.ReadPreference
import play.modules.reactivemongo.json._, ImplicitBSONHandlers._

@Singleton
class StockSentiment @Inject()(ws: WSClient) extends Controller with MongoController {

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  case class tweet(text: String, name: String)
  
  implicit val tweetReads = Json.reads[tweet]
  implicit val tweetWrites = new Writes[tweet] {
    def writes(t: tweet) = Json.obj(
        "text" -> t.text,
        "name" -> t.name
    )
  }
  
  private var tweetArray : Future[JsArray] = findTweets

  // mongodb collection
  def collection: JSONCollection = db.collection[JSONCollection]("tweets")
  
  // RABBITMQ ##############################################
  import akka.actor.ActorSystem
  import akka.actor.ActorRef

  import com.thenewmotion.akka.rabbitmq._
  implicit val system = ActorSystem()
  val factory = new ConnectionFactory()
  val rabbitConnection = system.actorOf(ConnectionActor.props(factory), "rabbitmq")
  val exchange = "amq.fanout"
  
  def setupSubscriber(channel: Channel, self: ActorRef) {
    val queue = channel.queueDeclare().getQueue
    channel.queueBind(queue, exchange, "")
    val consumer = new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: Array[Byte]) {
        println("received: " + fromBytes(body))
        tweetArray = findTweets
        // send websocket message here
      }
    }
    channel.basicConsume(queue, true, consumer)
  }
  rabbitConnection ! CreateChannel(ChannelActor.props(setupSubscriber), Some("subscriber"))
  
  
  def setupPublisher(channel: Channel, self: ActorRef) {
    val queue = channel.queueDeclare().getQueue
    channel.queueBind(queue, exchange, "")
  }
  rabbitConnection ! CreateChannel(ChannelActor.props(setupPublisher), Some("publisher"))


  def fromBytes(x: Array[Byte]) = new String(x, "UTF-8")
  def toBytes(x: Long) = x.toString.getBytes("UTF-8")
  // END RABBITMQ ############################################
  
    // return array containing latest tweets
  def getNewTweets = Action.async{
    tweetArray.map {
      tweets =>
        Ok(Json.toJson(tweets))
    }
  }
   
     // find newest tweets in mongodb and store them in array
  def findTweets : Future[JsArray] = {
    // let's do our query
    val cursor: Cursor[tweet] = collection.
      // find all
      find(Json.obj()).
      // perform the query and get a cursor of JsObject
      cursor[tweet](ReadPreference.primary)

    // gather all the JsObjects in a list
    val futureTweetsList: Future[List[tweet]] = cursor.collect[List]()

    // return list transformed into a JsArray
    return futureTweetsList.map { tweets =>
      Json.arr(tweets)
    }
  }
  
  // ############################################################################################

  /*def get(symbol: String): Action[AnyContent] = Action.async {
    logger.info(s"getting stock sentiment for $symbol")

    val futureStockSentiments: Future[Result] = for {
      tweets <- getTweets(symbol) // get tweets that contain the stock symbol
      futureSentiments = loadSentimentFromTweets(tweets.json) // queue web requests each tweets' sentiments
      sentiments <- Future.sequence(futureSentiments) // when the sentiment responses arrive, set them
    } yield Ok(sentimentJson(sentiments))

    futureStockSentiments.recover {
      case nsee: NoSuchElementException =>
        InternalServerError(Json.obj("error" -> JsString("Could not fetch the tweets")))
    }
  }

  private def getTextSentiment(text: String): Future[WSResponse] = {
    logger.info(s"getTextSentiment: text = $text")

    ws.url(sentimentUrl) post Map("text" -> Seq(text))
  }

  private def getAverageSentiment(responses: Seq[WSResponse], label: String): Double = {
    responses.map { response =>
      (response.json \\ label).head.as[Double]
    }.sum / responses.length.max(1)
  } // avoid division by zero

  private def loadSentimentFromTweets(json: JsValue): Seq[Future[WSResponse]] = {
    (json \ "statuses").as[Seq[tweet]] map (tweet => getTextSentiment(tweet.text))
  }

  private def getTweets(symbol:String): Future[WSResponse] = {
    logger.info(s"getTweets: symbol = $symbol")

    ws.url(tweetUrl.format(symbol)).get.withFilter { response =>
      response.status == OK
    }
  }

  private def sentimentJson(sentiments: Seq[WSResponse]): JsObject = {
    logger.info(s"sentimentJson: sentiments = $sentiments")

    val neg = getAverageSentiment(sentiments, "neg")
    val neutral = getAverageSentiment(sentiments, "neutral")
    val pos = getAverageSentiment(sentiments, "pos")

    val response = Json.obj(
      "probability" -> Json.obj(
        "neg" -> neg,
        "neutral" -> neutral,
        "pos" -> pos
      )
    )

    val classification =
      if (neutral > 0.5)
        "neutral"
      else if (neg > pos)
        "neg"
      else
        "pos"

    val r = response + ("label" -> JsString(classification))
    logger.info(s"response = $r")

    r
  }*/

}
