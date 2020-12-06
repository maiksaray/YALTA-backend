import org.scalatest.{AsyncWordSpec, MustMatchers, OptionValues, WordSpec}
import org.scalatestplus.play.WsScalaTestClient

abstract class AsyncPlaySpec extends AsyncWordSpec with MustMatchers with OptionValues with WsScalaTestClient{

}
