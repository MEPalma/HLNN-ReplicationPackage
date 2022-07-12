import common.HCode
import common.OHighlight
import common.OHighlight.Companion.overrideOf
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

fun ParseTree?.isTerminal(): TerminalNode? =
    if (this != null && this is TerminalNode) this else null


fun ParseTree?.isTerminal(ruleIndex: Int?): TerminalNode? =
    if (ruleIndex == null)
        this.isTerminal()
    else
        this.isTerminal()?.let {
            if (it.symbol.type == ruleIndex) it else null
        }

fun ParseTree?.isTerminal(ruleIndexes: Set<Int>?): TerminalNode? =
    if (ruleIndexes == null)
        this.isTerminal()
    else
        this.isTerminal()?.let {
            if (ruleIndexes.contains(it.symbol.type)) it else null
        }

fun ParseTree?.isProduction(): ParserRuleContext? =
    if (this != null && this is ParserRuleContext) this else null

fun ParseTree?.isProduction(ruleIndex: Int?): ParserRuleContext? =
    if (ruleIndex == null)
        this.isProduction()
    else
        this.isProduction()?.let {
            if (it.ruleIndex == ruleIndex) it else null
        }

fun ParseTree?.isProduction(ruleIndexes: Set<Int>?): ParserRuleContext? =
    if (ruleIndexes == null)
        this.isProduction()
    else
        this.isProduction()?.let {
            if (ruleIndexes.contains(it.ruleIndex)) it else null
        }

fun ParserRuleContext?.loopingOnChildren(
    parserVocab: Array<String>,
    addReplacingFunc: (OHighlight) -> Unit,
    onTerminal: (TerminalNode) -> HCode? = { _ -> null },
    targetTerminalIndex: Int? = null,
    onProduction: (ParserRuleContext) -> HCode? = { _ -> null },
    targetProductionIndex: Int? = null,
    onAddedExit: Boolean = false,
    reversed: Boolean = false,
) {
    (if (reversed) this?.children?.reversed() else this?.children)?.forEach { mpt ->
        (mpt.isTerminal(targetTerminalIndex)
            ?.let { t -> onTerminal(t)?.let { hc -> overrideOf(t, hc, this!!.ruleIndex, parserVocab) } }
            ?: mpt.isProduction(targetProductionIndex)
                ?.let { p -> onProduction(p)?.let { hc -> overrideOf(p, hc, this!!.ruleIndex, parserVocab) } }
                )?.let { oh ->
                addReplacingFunc(oh)
                if (onAddedExit) return
            }
    }
}
