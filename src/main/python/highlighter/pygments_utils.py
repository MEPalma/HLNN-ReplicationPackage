import sys
import json
from pygments.formatter import Formatter
import utils as utils


class DropFormatter(Formatter):
    name = 'DropFormatter'
    aliases = ['DropFormatter']
    filenames = ['*.*']

    def __init__(self, **options):
        Formatter.__init__(self, **options)

    def formatter(self, tokensource, outfile):
        pass

    def format_unencoded(self, tokensource, outfile):
        self.formatter(tokensource, outfile)


class JSONFormatter(Formatter):
    name = 'JSONFormatter'
    aliases = ['JSONFormatter']
    filenames = ['*.*']

    def __init__(self, bindings, **options):
        Formatter.__init__(self, **options)
        self.bindings = bindings

    def formatter(self, tokensource, outfile):
        sols = []
        for ttype, value in tokensource:
            sttype = str(ttype)
            sols.append((str(value), sttype, to_oracle_bindings(sttype, bindings=self.bindings)))
        outfile.write(json.dumps(sols, sort_keys=False))
        outfile.flush()

    def format_unencoded(self, tokensource, outfile):
        self.formatter(tokensource, outfile)


# Pygments to HCode class bindings.
#
# HCodes class IDs.
__ANY__=                     utils.ANY[0]
__KEYWORD__ =                utils.KEYWORD[0]
__LITERAL__ =                utils.LITERAL[0]
__STRING_LIT__ =             utils.CHAR_STRING_LITERAL[0]
__COMMENT__ =                utils.COMMENT[0]
__CLASS_DECLARATOR__ =       utils.CLASS_DECLARATOR[0]
__FUNCTION_DECLARATOR__ =    utils.FUNCTION_DECLARATOR[0]
__VARIABLE_DECLARATOR__ =    utils.VARIABLE_DECLARATOR[0]
__TYPE_IDENTIFIER__ =        utils.TYPE_IDENTIFIER[0]
__FUNCTION_IDENTIFIER__ =    utils.FUNCTION_IDENTIFIER[0]
__FIELD_IDENTIFIER__ =       utils.FIELD_IDENTIFIER[0]
__ANNOTATION_DECLARATOR__ =  utils.ANNOTATION_DECLARATOR[0]
#
__UNKNOWN_PYGMENT_TOKEN_TYPE = -1
#
# As reported from:
#   https://pygments.org/docs/tokens/#module-pygments.token
#   last reviewed on the 21/08/2021.
__TO_ORACLE_BASE_BINDINGS__ = {
    # Keyword Tokens.
    # For any kind of keyword (especially if it doesn’t match any of the subtypes of course).
    "Token.Keyword":                            __KEYWORD__,
    # For keywords that are constants (e.g. None in future Python versions).
    "Token.Keyword.Constant":                   __KEYWORD__,
    # For keywords used for variable declaration (e.g. var in some programming languages like JavaScript).
    "Token.Keyword.Declaration":                __KEYWORD__,
    # For keywords used for namespace declarations (e.g. import in Python and Java and package in Java).
    "Token.Keyword.Namespace":                  __KEYWORD__,
    # For keywords that aren’t really keywords (e.g. None in old Python versions).
    "Token.Keyword.Pseudo":                     __KEYWORD__,
    # For reserved keywords.
    "Token.Keyword.Reserved":                   __KEYWORD__,
    # For builtin types that can’t be used as identifiers (e.g. int, char etc. in C).
    "Token.Keyword.Type":                       __KEYWORD__,
    # Name Tokens.
    # For any name (variable names, function names, classes).
    "Token.Name":                               __ANY__,
    # For all attributes (e.g. in HTML tags).
    "Token.Name.Attribute":                     __FIELD_IDENTIFIER__,
    # Builtin names; names that are available in the global namespace.
    "Token.Name.Builtin":                       __ANY__,
    # Builtin names that are implicit (e.g. self in Ruby, this in Java).
    "Token.Name.Builtin.Pseudo":                __KEYWORD__,
    # Class names. Because no lexer can know if a name is a class or a function or something else this
    # token is meant for class declarations.
    "Token.Name.Class":                         __CLASS_DECLARATOR__,
    # Token type for constants. In some languages you can recognise a token by the way it’s defined
    # (the value after a const keyword for example). In other languages constants are uppercase by definition (Ruby).
    "Token.Name.Constant":                      __VARIABLE_DECLARATOR__,
    # Token type for decorators. Decorators are syntactic elements in the Python language. Similar syntax
    # elements exist in C# and Java.
    "Token.Name.Decorator":                     __ANNOTATION_DECLARATOR__,
    # Token type for special entities. (e.g. &nbsp; in HTML).
    "Token.Name.Entity":                        __ANY__,
    # Token type for exception names (e.g. RuntimeError in Python). Some languages define exceptions in the
    # function signature (Java). You can highlight the name of that exception using this token then.
    "Token.Name.Exception":                     __TYPE_IDENTIFIER__,
    # Token type for function names.
    "Token.Name.Function":                      __FUNCTION_IDENTIFIER__,
    # same as Name.Function but for special function names that have an implicit use in a language
    # (e.g. __init__ method in Python).
    "Token.Name.Function.Magic":                __FUNCTION_IDENTIFIER__,
    # Token type for label names (e.g. in languages that support goto).
    "Token.Name.Label":                         __KEYWORD__,
    # Token type for namespaces. (e.g. import paths in Java/Python), names following the module/namespace
    # keyword in other languages.
    "Token.Name.Namespace":                     __KEYWORD__,
    # Other names. Normally unused.
    "Token.Name.Other":                         __ANY__,
    # Property.
    "Token.Name.Property":                      __ANY__,
    # Tag names (in HTML/XML markup or configuration files).
    "Token.Name.Tag":                           __ANY__,
    # Token type for variables. Some languages have prefixes for variable names (PHP, Ruby, Perl).
    "Token.Name.Variable":                      __ANY__,
    # Same as Name.Variable but for class variables (also static variables).
    "Token.Name.Variable.Class":                __ANY__,
    # Same as Name.Variable but for global variables (used in Ruby, for example).
    "Token.Name.Variable.Global":               __ANY__,
    # Same as Name.Variable but for instance variables.
    "Token.Name.Variable.Instance":             __ANY__,
    # Same as Name.Variable but for special variable names that have an implicit use in a
    # language (e.g. __doc__ in Python).
    "Token.Name.Variable.Magic":                __ANY__,
    # Literals.
    # For any literal (if not further defined).
    "Token.Literal":                            __LITERAL__,
    # For date literals (e.g. 42d in Boo).
    "Token.Literal.Date":                       __ANY__,
    # For any string literal.
    "Token.Literal.String":                     __STRING_LIT__,
    # Token type for affixes that further specify the type of the string they’re attached to
    # (e.g. the prefixes r and u8 in r"foo" and u8"foo").
    "Token.Literal.String.Affix":                __STRING_LIT__,
    # Token type for strings enclosed in backticks.
    "Token.Literal.String.Backtick":            __STRING_LIT__,
    # Token type for single characters (e.g. Java, C).
    "Token.Literal.String.Char":                __STRING_LIT__,
    # Token type for delimiting identifiers in “heredoc”, raw and other similar strings
    # (e.g. the word END in Perl code print <<'END';).
    "Token.Literal.String.Delimiter":           __STRING_LIT__,
    # Token type for documentation strings (for example Python).
    "Token.Literal.String.Doc":                 __STRING_LIT__,
    # Double quoted strings.
    "Token.Literal.String.Double":              __STRING_LIT__,
    # Token type for escape sequences in strings.
    "Token.Literal.String.Escape":              __STRING_LIT__,
    # Token type for “heredoc” strings (e.g. in Ruby or Perl).
    "Token.Literal.String.Heredoc":             __STRING_LIT__,
    # Token type for interpolated parts in strings (e.g. #{foo} in Ruby).
    "Token.Literal.String.Interpol":            __STRING_LIT__,
    # Token type for any other strings (for example %q{foo} string constructs in Ruby).
    "Token.Literal.String.Other":               __STRING_LIT__,
    # Token type for regular expression literals (e.g. /foo/ in JavaScript).
    "Token.Literal.String.Regex":               __STRING_LIT__,
    # Token type for single quoted strings.
    "Token.Literal.String.Single":              __STRING_LIT__,
    # Token type for symbols (e.g. :foo in LISP or Ruby).
    "Token.Literal.String.Symbol":              __STRING_LIT__,
    # Token type for any number literal.
    "Token.Literal.Number":                     __LITERAL__,
    # Token type for binary literals (e.g. 0b101010).
    "Token.Literal.Number.Bin":                 __LITERAL__,
    # Token type for float literals (e.g. 42.0).
    "Token.Literal.Number.Float":               __LITERAL__,
    # Token type for hexadecimal number literals (e.g. 0xdeadbeef).
    "Token.Literal.Number.Hex":                 __LITERAL__,
    # Token type for integer literals (e.g. 42).
    "Token.Literal.Number.Integer":             __LITERAL__,
    # Token type for long integer literals (e.g. 42L in Python).
    "Token.Literal.Number.Integer.Long":        __LITERAL__,
    # Token type for octal literals.
    "Token.Literal.Number.Oct":                 __LITERAL__,
    # Operators.
    # For any punctuation operator (e.g. +, -).
    "Token.Operator":                           __ANY__,
    # For any operator that is a word (e.g. not).
    "Token.Operator.Word":                      __KEYWORD__,
    # Punctuation.
    # For any punctuation which is not an operator (e.g. [, (…)
    "Token.Punctuation":                        __ANY__,
    # For markers that point to a location (e.g., carets in Python tracebacks for syntax errors).
    "Token.Punctuation.Marker":                 __ANY__,
    # Comments.
    # Token type for any comment.
    "Token.Comment":                            __COMMENT__,
    # Token type for hashbang comments (i.e. first lines of files that start with #!).
    "Token.Comment.Hashbang":                   __ANY__,
    # Token type for multiline comments.
    "Token.Comment.Multiline":                  __COMMENT__,
    # Token type for preprocessor comments (also <?php/<% constructs).
    "Token.Comment.Preproc":                    __COMMENT__,
    # Token type for comments that end at the end of a line (e.g. # foo).
    "Token.Comment.Single":                     __COMMENT__,
    # Special data in comments. For example code tags, author and license information, etc.
    "Token.Comment.Special":                    __COMMENT__,
    # Generic Tokens.
    # A generic, unstyled token. Normally you don’t use this token type.
    "Token.Generic":                            __ANY__,
    # Marks the token value as deleted.
    "Token.Generic.Deleted":                    __ANY__,
    # Marks the token value as emphasized.
    "Token.Generic.Emph":                       __ANY__,
    # Marks the token value as an error message.
    "Token.Generic.Error":                      __ANY__,
    # Marks the token value as headline.
    "Token.Generic.Heading":                    __ANY__,
    # Marks the token value as inserted.
    "Token.Generic.Inserted":                   __ANY__,
    # Marks the token value as program output (e.g. for python cli lexer).
    "Token.Generic.Output":                     __ANY__,
    # Marks the token value as command prompt (e.g. bash lexer).
    "Token.Generic.Prompt":                     __ANY__,
    # Marks the token value as bold (e.g. for rst lexer).
    "Token.Generic.Strong":                     __ANY__,
    # Marks the token value as subheadline.
    "Token.Generic.Subheading":                 __ANY__,
    # Marks the token value as a part of an error traceback.
    "Token.Generic.Traceback":                  __ANY__,
    # Others.
    "Token":                                    __ANY__,
    # For any type of text data.
    "Token.Text":                               __ANY__,
    # For specially highlighted whitespace.
    "Token.Text.Whitespace":                    __ANY__,
    # Represents lexer errors
    "Token.Error":                              __ANY__,
    # Special token for data not matched by a parser (e.g. HTML markup in PHP code)
    "Token.Other":                              __ANY__,
}


# Custom bindings per language.
JAVA_ORACLE_BINDINGS =    __TO_ORACLE_BASE_BINDINGS__
KOTLIN_ORACLE_BINDINGS =  __TO_ORACLE_BASE_BINDINGS__
PYTHON3_ORACLE_BINDINGS = __TO_ORACLE_BASE_BINDINGS__


def to_oracle_bindings(pygment_token_type: str, bindings=__TO_ORACLE_BASE_BINDINGS__) -> int:
    ob = bindings.get(pygment_token_type, __UNKNOWN_PYGMENT_TOKEN_TYPE)
    if ob == __UNKNOWN_PYGMENT_TOKEN_TYPE:
        print(f"Unrecognised Pygment token type {pygment_token_type}", file=sys.stderr)
        ob = __ANY__
    return ob
