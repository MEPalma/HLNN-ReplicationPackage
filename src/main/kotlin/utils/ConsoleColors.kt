package utils

const val RESET = "\u001b[0m"

const val BLACK = "\u001b[0;30m"
const val RED = "\u001b[0;31m"
const val GREEN = "\u001b[0;32m"
const val YELLOW = "\u001b[0;33m"
const val BLUE = "\u001b[0;34m"
const val PURPLE = "\u001b[0;35m"
const val CYAN = "\u001b[0;36m"
const val WHITE = "\u001b[0;37m"

const val BLACK_BOLD = "\u001b[1;30m"
const val RED_BOLD = "\u001b[1;31m"
const val GREEN_BOLD = "\u001b[1;32m"
const val YELLOW_BOLD = "\u001b[1;33m"
const val BLUE_BOLD = "\u001b[1;34m"
const val PURPLE_BOLD = "\u001b[1;35m"
const val CYAN_BOLD = "\u001b[1;36m"
const val WHITE_BOLD = "\u001b[1;37m"

const val BLACK_ITALIC = "\u001b[3;30m"
const val RED_ITALIC = "\u001b[3;31m"
const val GREEN_ITALIC = "\u001b[3;32m"
const val YELLOW_ITALIC = "\u001b[3;33m"
const val BLUE_ITALIC = "\u001b[3;34m"
const val PURPLE_ITALIC = "\u001b[3;35m"
const val CYAN_ITALIC = "\u001b[3;36m"
const val WHITE_ITALIC = "\u001b[3;37m"

const val BLACK_UNDERLINED = "\u001b[4;30m"
const val RED_UNDERLINED = "\u001b[4;31m"
const val GREEN_UNDERLINED = "\u001b[4;32m"
const val YELLOW_UNDERLINED = "\u001b[4;33m"
const val BLUE_UNDERLINED = "\u001b[4;34m"
const val PURPLE_UNDERLINED = "\u001b[4;35m"
const val CYAN_UNDERLINED = "\u001b[4;36m"
const val WHITE_UNDERLINED = "\u001b[4;37m"

const val BLACK_BACKGROUND = "\u001b[40m"
const val RED_BACKGROUND = "\u001b[41m"
const val GREEN_BACKGROUND = "\u001b[42m"
const val YELLOW_BACKGROUND = "\u001b[43m"
const val BLUE_BACKGROUND = "\u001b[44m"
const val PURPLE_BACKGROUND = "\u001b[45m"
const val CYAN_BACKGROUND = "\u001b[46m"
const val WHITE_BACKGROUND = "\u001b[47m"

const val BLACK_BRIGHT = "\u001b[0;90m"
const val RED_BRIGHT = "\u001b[0;91m"
const val GREEN_BRIGHT = "\u001b[0;92m"
const val YELLOW_BRIGHT = "\u001b[0;93m"
const val BLUE_BRIGHT = "\u001b[0;94m"
const val PURPLE_BRIGHT = "\u001b[0;95m"
const val CYAN_BRIGHT = "\u001b[0;96m"
const val WHITE_BRIGHT = "\u001b[0;97m"

const val BLACK_ITALIC_BRIGHT = "\u001b[3;90m"
const val RED_ITALIC_BRIGHT = "\u001b[3;91m"
const val GREEN_ITALIC_BRIGHT = "\u001b[3;92m"
const val YELLOW_ITALIC_BRIGHT = "\u001b[3;93m"
const val BLUE_ITALIC_BRIGHT = "\u001b[3;94m"
const val PURPLE_ITALIC_BRIGHT = "\u001b[3;95m"
const val CYAN_ITALIC_BRIGHT = "\u001b[3;96m"
const val WHITE_ITALIC_BRIGHT = "\u001b[3;97m"

const val BLACK_BOLD_BRIGHT = "\u001b[1;90m"
const val RED_BOLD_BRIGHT = "\u001b[1;91m"
const val GREEN_BOLD_BRIGHT = "\u001b[1;92m"
const val YELLOW_BOLD_BRIGHT = "\u001b[1;93m"
const val BLUE_BOLD_BRIGHT = "\u001b[1;94m"
const val PURPLE_BOLD_BRIGHT = "\u001b[1;95m"
const val CYAN_BOLD_BRIGHT = "\u001b[1;96m"
const val WHITE_BOLD_BRIGHT = "\u001b[1;97m"

const val BLACK_BACKGROUND_BRIGHT = "\u001b[0;100m"
const val RED_BACKGROUND_BRIGHT = "\u001b[0;101m"
const val GREEN_BACKGROUND_BRIGHT = "\u001b[0;102m"
const val YELLOW_BACKGROUND_BRIGHT = "\u001b[0;103m"
const val BLUE_BACKGROUND_BRIGHT = "\u001b[0;104m"
const val PURPLE_BACKGROUND_BRIGHT = "\u001b[0;105m"
const val CYAN_BACKGROUND_BRIGHT = "\u001b[0;106m"
const val WHITE_BACKGROUND_BRIGHT = "\u001b[0;107m"

fun Any?.printIn(color: String) =
    print("$color$this$RESET")

fun Any?.print() =
    this.printIn(RESET)

fun Any?.printlnIn(color: String) =
    println("$color$this$RESET")

fun Any?.println() =
    this.printlnIn(RESET)

fun String.inColor(color: String) =
    "$color$this$RESET"
