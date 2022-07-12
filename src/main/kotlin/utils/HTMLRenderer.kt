package utils

import common.HCode

fun String.inHTMLFile(): String =
    """
<!DOCTYPE html>
<html>
<style>
.ANY {
    color: black;
    font-weight: normal;
    font-style: normal;
}
.KEYWORD {
    color: blue;
    font-weight: bold;
    font-style: normal;
}
.LITERAL {
    color: lightskyblue;
    font-weight: bold;
    font-style: normal;
}
.CHAR_STRING_LITERAL {
    color: darkgoldenrod;
    font-weight: normal;
    font-style: normal;
}
.COMMENT {
    color: grey;
    font-weight: normal;
    font-style: italic;
}
.CLASS_DECLARATOR {
    color: crimson;
    font-weight: bold;
    font-style: normal;
}
.FUNCTION_DECLARATOR {
    color: fuchsia;
    font-weight: bold;
    font-style: normal;
}
.VARIABLE_DECLARATOR {
    color: purple;
    font-weight: bold;
    font-style: normal;
}
.TYPE_IDENTIFIER {
    color: darkgreen;
    font-weight: bold;
    font-style: normal;
}
.FUNCTION_IDENTIFIER {
    color: dodgerblue;
    font-weight: normal;
    font-style: normal;
}
.FIELD_IDENTIFIER {
    color: coral;
    font-weight: normal;
    font-style: normal;
}
.ANNOTATION_DECLARATOR {
    color: lightslategray;
    font-weight: lighter;
    font-style: italic;
}
</style>
<pre>
$this
</pre>
</html>
""".trimIndent()

fun String.inHTML(hcode: HCode): String =
    hcode.name.let { tag -> "<code class=\"$tag\">$this</code>" }
