package highlighter.kotlinhighlighter

import KotlinLexer
import KotlinParser
import KotlinParserBaseListener
import common.HCode
import common.OHighlight
import highlighter.GrammaticalHighlighter
import isProduction
import isTerminal
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode
import java.util.*

class KotlinGrammaticalHighlighter : KotlinParserBaseListener(), GrammaticalHighlighter {

    private val oHighlights = hashMapOf<Int, OHighlight>()

    private fun OHighlight.addReplacing() {
        oHighlights[this.startIndex] = this
    }

    override fun getOverrides(): Collection<OHighlight> =
        this.oHighlights.values

    override fun reset() {
        oHighlights.clear()
    }

    private fun overrideOf(terminalNode: TerminalNode, hCode: HCode, overridingRuleIndex: Int): OHighlight =
        OHighlight(
            startIndex = terminalNode.symbol.startIndex,
            stopIndex = terminalNode.symbol.stopIndex,
            highlightCode = hCode.ordinal,
            highlightColor = hCode.colorCode,
            overridingRule = KotlinParser.ruleNames[overridingRuleIndex]
        )

    private fun overrideOf(prc: ParserRuleContext, hCode: HCode, overridingRuleIndex: Int): OHighlight =
        OHighlight(
            startIndex = prc.start.startIndex,
            stopIndex = prc.stop.stopIndex,
            highlightCode = hCode.ordinal,
            highlightColor = hCode.colorCode,
            overridingRule = KotlinParser.ruleNames[overridingRuleIndex]
        )

    private fun ParserRuleContext?.loopingOnChildren(
        onTerminal: (TerminalNode) -> HCode? = { _ -> null },
        targetTerminalIndex: Int? = null,
        onProduction: (ParserRuleContext) -> HCode? = { _ -> null },
        targetProductionIndex: Int? = null,
        onAddedExit: Boolean = false
    ) {
        this?.children?.forEach { mpt ->
            (mpt.isTerminal(targetTerminalIndex)
                ?.let { t -> onTerminal(t)?.let { hc -> overrideOf(t, hc, this.ruleIndex) } } ?: mpt.isProduction(
                targetProductionIndex
            )?.let { p -> onProduction(p)?.let { hc -> overrideOf(p, hc, this.ruleIndex) } }
                    )?.let { oh ->
                    oh.addReplacing()
                    if (onAddedExit) return
                }
        }
    }

    private fun overrideSimpleUserTypeWithTargetCode(ctx: ParserRuleContext?, targetHCode: HCode) =
        ctx.loopingOnChildren(
            targetProductionIndex = KotlinParser.RULE_simpleIdentifier,
            onProduction = { targetHCode },
            onAddedExit = true
        )

    override fun exitClassDeclaration(ctx: KotlinParser.ClassDeclarationContext?) {
        var isFunDeclaration = false // If false then this is a class or interface declaration.
        ctx.loopingOnChildren(
            onTerminal = { isFunDeclaration = true; null },
            targetTerminalIndex = KotlinLexer.FUN,
            onProduction = { if (isFunDeclaration) HCode.FUNCTION_DECLARATOR else HCode.CLASS_DECLARATOR },
            targetProductionIndex = KotlinParser.RULE_simpleIdentifier,
            onAddedExit = true
        )
    }

    override fun exitCompanionObject(ctx: KotlinParser.CompanionObjectContext?) =
        ctx.loopingOnChildren(
            targetProductionIndex = KotlinParser.RULE_simpleIdentifier,
            onProduction = { HCode.CLASS_DECLARATOR },
            onAddedExit = true
        )

    override fun exitObjectDeclaration(ctx: KotlinParser.ObjectDeclarationContext?) =
        ctx.loopingOnChildren(
            targetProductionIndex = KotlinParser.RULE_simpleIdentifier,
            onProduction = { HCode.CLASS_DECLARATOR },
            onAddedExit = true
        )

    override fun exitFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext?) =
        ctx.loopingOnChildren(
            targetProductionIndex = KotlinParser.RULE_simpleIdentifier,
            onProduction = { HCode.FUNCTION_DECLARATOR },
            onAddedExit = true
        )

    override fun exitLambdaLiteral(ctx: KotlinParser.LambdaLiteralContext?) {
        ctx?.children?.get(0)?.isTerminal(KotlinLexer.LCURL)?.let {
            overrideOf(it, HCode.FUNCTION_DECLARATOR, KotlinParser.RULE_lambdaLiteral).addReplacing()
        }
        ctx?.children?.get(ctx.childCount - 1)?.isTerminal(KotlinLexer.RCURL)?.let {
            overrideOf(it, HCode.FUNCTION_DECLARATOR, KotlinParser.RULE_lambdaLiteral).addReplacing()
        }
    }

    override fun exitTypeAlias(ctx: KotlinParser.TypeAliasContext?) =
        ctx.loopingOnChildren(
            targetProductionIndex = KotlinParser.RULE_simpleIdentifier,
            onProduction = { HCode.CLASS_DECLARATOR },
            onAddedExit = true
        )

    override fun exitVariableDeclaration(ctx: KotlinParser.VariableDeclarationContext?) =
        ctx.loopingOnChildren(
            targetProductionIndex = KotlinParser.RULE_simpleIdentifier,
            onProduction = { HCode.VARIABLE_DECLARATOR },
            onAddedExit = true
        )

    override fun exitEnumEntry(ctx: KotlinParser.EnumEntryContext?) {
        ctx.loopingOnChildren(
            targetProductionIndex = KotlinParser.RULE_simpleIdentifier,
            onProduction = { HCode.CLASS_DECLARATOR },
            onAddedExit = true
        )
    }

    override fun exitSimpleUserType(ctx: KotlinParser.SimpleUserTypeContext?) =
        overrideSimpleUserTypeWithTargetCode(ctx, HCode.TYPE_IDENTIFIER)

    override fun exitNullableType(ctx: KotlinParser.NullableTypeContext?) =
        ctx.loopingOnChildren(
            targetProductionIndex = KotlinParser.RULE_quest,
            onProduction = { HCode.TYPE_IDENTIFIER },
            onAddedExit = false
        )

    override fun exitPostfixUnaryExpression(ctx: KotlinParser.PostfixUnaryExpressionContext?) {
        val stack = Stack<Triple<ParserRuleContext, Int, HCode?>>()
        ctx.loopingOnChildren(
            onAddedExit = false,
            onProduction = { p ->
                p.isProduction(KotlinParser.RULE_primaryExpression)
                    ?.let { stack.push(Triple(it, KotlinParser.RULE_primaryExpression, null)) } ?: p.isProduction(
                    KotlinParser.RULE_postfixUnarySuffix
                )?.children?.get(0)?.let { pusc ->
                    pusc.isProduction(KotlinParser.RULE_typeArguments)?.let { _ ->
                        stack.push(stack.pop().copy(third = HCode.TYPE_IDENTIFIER)) // class_type<...>
                        // Skip rule.
                    } ?: pusc.isProduction(KotlinParser.RULE_callSuffix)?.let { _ ->
                        stack.push(stack.pop().copy(third = HCode.FUNCTION_IDENTIFIER))
                        // Skip rule.
                    } ?: pusc.isProduction(KotlinParser.RULE_navigationSuffix)?.let { ns ->
                        ns.loopingOnChildren(
                            targetProductionIndex = KotlinParser.RULE_simpleIdentifier,
                            onProduction = { nssi ->
                                stack.push(Triple(nssi, KotlinParser.RULE_navigationSuffix, HCode.FIELD_IDENTIFIER))
                                null
                            }
                        )
                    }
                }
                null
            }
        )
        stack.forEach {
            if (it.third != null)
                overrideOf(it.first, it.third!!, it.second).addReplacing()
        }
    }

    override fun exitAssignableSuffix(ctx: KotlinParser.AssignableSuffixContext?) =
        ctx.loopingOnChildren(
            targetProductionIndex = KotlinParser.RULE_navigationSuffix,
            onProduction = { p ->
                p.loopingOnChildren(
                    targetProductionIndex = KotlinParser.RULE_simpleIdentifier,
                    onProduction = { HCode.FIELD_IDENTIFIER }
                )
                null
            }
        )

    override fun exitSingleAnnotation(ctx: KotlinParser.SingleAnnotationContext?) {
        if (ctx != null)
            for (c in ctx.children)
                if (c?.isProduction(KotlinParser.RULE_unescapedAnnotation) != null)
                    return
                else
                    c.isProduction()
                        ?.let { overrideOf(it, HCode.ANNOTATION_DECLARATOR, KotlinParser.RULE_annotationUseSiteTarget) }
                        ?: c.isTerminal()?.let {
                            overrideOf(
                                it,
                                HCode.ANNOTATION_DECLARATOR,
                                KotlinParser.RULE_annotationUseSiteTarget
                            )
                        }
                            ?.addReplacing()
    }

    override fun exitMultiAnnotation(ctx: KotlinParser.MultiAnnotationContext?) {
        if (ctx != null)
            for (c in ctx.children)
                if (c?.isTerminal(KotlinLexer.LSQUARE) != null)
                    return
                else
                    c.isProduction()
                        ?.let { overrideOf(it, HCode.ANNOTATION_DECLARATOR, KotlinParser.RULE_annotationUseSiteTarget) }
                        ?: c.isTerminal()?.let {
                            overrideOf(
                                it,
                                HCode.ANNOTATION_DECLARATOR,
                                KotlinParser.RULE_annotationUseSiteTarget
                            )
                        }
                            ?.addReplacing()
    }

    override fun exitUnescapedAnnotation(ctx: KotlinParser.UnescapedAnnotationContext?) =
        ctx.loopingOnChildren(
            onProduction = { p ->
                p.isProduction(KotlinParser.RULE_userType)?.let { ut ->
                    ut.loopingOnChildren(
                        targetProductionIndex = KotlinParser.RULE_simpleUserType,
                        onProduction = { overrideSimpleUserTypeWithTargetCode(it, HCode.ANNOTATION_DECLARATOR); null }
                    )
                } ?: p.isProduction(KotlinParser.RULE_constructorInvocation)?.let { ci ->
                    ci.loopingOnChildren(
                        targetProductionIndex = KotlinParser.RULE_userType,
                        onProduction = { ut ->
                            ut.loopingOnChildren(
                                targetProductionIndex = KotlinParser.RULE_simpleUserType,
                                onProduction = {
                                    overrideSimpleUserTypeWithTargetCode(
                                        it,
                                        HCode.ANNOTATION_DECLARATOR
                                    ); null
                                }
                            )
                            null
                        }
                    )
                }
                null
            }
        )
}
