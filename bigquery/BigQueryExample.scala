//> using scala 3
//> using platform js

import scala.scalajs.js
import scala.scalajs.js.annotation.*

enum Flavor:
  case 🥬, 🐓, 🐄, 🦐, 🐖

@JSExportTopLevel("dailyFlavor")
def dailyFlavor(b: String): String =
  Flavor.valueOf(b) match
    case Flavor.`🥬` => "Veggie day"
    case Flavor.`🦐` => "Seafood lover!"
    case _ => "...and so on"
  
@main def main(): Unit =
  println(dailyFlavor("🐖"))
