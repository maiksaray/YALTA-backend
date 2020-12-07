import org.scalatest.{AsyncWordSpec, MustMatchers, OptionValues, WordSpec}
import org.scalatestplus.play.WsScalaTestClient

/**
 * This is custom implementation of PlaySpec, but extending AsyncWordSpec instead of WordSpec
 * This does not work properly now for number of reasons
 * https://github.com/playframework/scalatestplus-play/issues/112
 * Left here for future references
 */
abstract class AsyncPlaySpec extends AsyncWordSpec with MustMatchers with OptionValues with WsScalaTestClient{

}
