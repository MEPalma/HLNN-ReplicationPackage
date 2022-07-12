package highlighter.javahighlighter

import Java8Lexer
import Java8Parser
import Java8ParserBaseListener
import common.HCode
import common.OHighlight
import common.OHighlight.Companion.overrideOf
import highlighter.GrammaticalHighlighter
import loopingOnChildren
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode
import java.util.*

class JavaGrammaticalHighlighter : GrammaticalHighlighter, Java8ParserBaseListener() {
    private val oHighlights = hashMapOf<Int, OHighlight>()

    private fun OHighlight.addReplacing() {
        oHighlights[this.startIndex] = this
    }

    override fun getOverrides(): Collection<OHighlight> =
        this.oHighlights.values

    override fun reset() {
        this.oHighlights.clear()
    }

    private fun ParserRuleContext?.myLoopingOnChildren(
        onTerminal: (TerminalNode) -> HCode? = { _ -> null },
        targetTerminalIndex: Int? = null,
        onProduction: (ParserRuleContext) -> HCode? = { _ -> null },
        targetProductionIndex: Int? = null,
        onAddedExit: Boolean = false,
        reversed: Boolean = false,
    ) =
        this.loopingOnChildren(
            parserVocab = Java8Parser.ruleNames,
            addReplacingFunc = { it.addReplacing() },
            onTerminal = onTerminal,
            targetTerminalIndex = targetTerminalIndex,
            onProduction = onProduction,
            targetProductionIndex = targetProductionIndex,
            onAddedExit = onAddedExit,
            reversed = reversed
        )

    private fun assignOnFirstIdentifier(ctx: ParserRuleContext?, hcode: HCode) =
        ctx.myLoopingOnChildren(
            targetTerminalIndex = Java8Lexer.Identifier,
            onTerminal = { hcode },
            onAddedExit = true
        )

    // +-----------------+
    // |  DECLARATIONS  |
    //+-----------------+

    override fun exitNormalClassDeclaration(ctx: Java8Parser.NormalClassDeclarationContext?) =
        assignOnFirstIdentifier(ctx, HCode.CLASS_DECLARATOR)

    override fun exitEnumDeclaration(ctx: Java8Parser.EnumDeclarationContext?) =
        assignOnFirstIdentifier(ctx, HCode.CLASS_DECLARATOR)

    override fun exitNormalInterfaceDeclaration(ctx: Java8Parser.NormalInterfaceDeclarationContext?) =
        assignOnFirstIdentifier(ctx, HCode.CLASS_DECLARATOR)

    override fun exitAnnotationTypeDeclaration(ctx: Java8Parser.AnnotationTypeDeclarationContext?) =
        assignOnFirstIdentifier(ctx, HCode.CLASS_DECLARATOR)

    override fun exitEnumConstant(ctx: Java8Parser.EnumConstantContext?) =
        assignOnFirstIdentifier(ctx, HCode.CLASS_DECLARATOR)

    override fun exitVariableDeclaratorId(ctx: Java8Parser.VariableDeclaratorIdContext?) =
        assignOnFirstIdentifier(ctx, HCode.VARIABLE_DECLARATOR)

    override fun exitMethodDeclarator(ctx: Java8Parser.MethodDeclaratorContext?) =
        assignOnFirstIdentifier(ctx, HCode.FUNCTION_DECLARATOR)

    override fun exitConstructorDeclarator(ctx: Java8Parser.ConstructorDeclaratorContext?) =
        ctx.myLoopingOnChildren(
            targetProductionIndex = Java8Parser.RULE_simpleTypeName,
            onProduction = { HCode.FUNCTION_DECLARATOR }
        )

    // +----------+
    // |  TYPES  |
    //+----------+
    override fun exitClassType(ctx: Java8Parser.ClassTypeContext?) =
        assignOnFirstIdentifier(ctx, HCode.TYPE_IDENTIFIER)

    override fun exitTypeVariable(ctx: Java8Parser.TypeVariableContext?) =
        assignOnFirstIdentifier(ctx, HCode.TYPE_IDENTIFIER)

    override fun exitTypeParameter(ctx: Java8Parser.TypeParameterContext?) =
        assignOnFirstIdentifier(ctx, HCode.TYPE_IDENTIFIER)

    override fun exitClassType_lf_classOrInterfaceType(ctx: Java8Parser.ClassType_lf_classOrInterfaceTypeContext?) =
        assignOnFirstIdentifier(ctx, HCode.TYPE_IDENTIFIER)

    override fun exitClassType_lfno_classOrInterfaceType(ctx: Java8Parser.ClassType_lfno_classOrInterfaceTypeContext?) =
        assignOnFirstIdentifier(ctx, HCode.TYPE_IDENTIFIER)

    override fun exitUnannClassType_lf_unannClassOrInterfaceType(ctx: Java8Parser.UnannClassType_lf_unannClassOrInterfaceTypeContext?) =
        assignOnFirstIdentifier(ctx, HCode.TYPE_IDENTIFIER)

    override fun exitUnannClassType_lfno_unannClassOrInterfaceType(ctx: Java8Parser.UnannClassType_lfno_unannClassOrInterfaceTypeContext?) =
        assignOnFirstIdentifier(ctx, HCode.TYPE_IDENTIFIER)

    override fun exitUnannTypeVariable(ctx: Java8Parser.UnannTypeVariableContext?) =
        assignOnFirstIdentifier(ctx, HCode.TYPE_IDENTIFIER)

    // Not highlighting import statements as field accesses.
    /*
    // Skip as too ambiguous is referes to type chain of field accesses -- from a semantic point of view..
    override fun exitTypeName(ctx: Java8Parser.TypeNameContext?) {
        if (ctx != null) {
            if (!hashSetOf(
                    Java8Parser.RULE_importDeclaration, Java8Parser.RULE_singleStaticImportDeclaration,
                    Java8Parser.RULE_staticImportOnDemandDeclaration, Java8Parser.RULE_typeImportOnDemandDeclaration,
                    Java8Parser.RULE_singleTypeImportDeclaration
                ).contains(ctx.parent.ruleIndex)) {
                val accessSeq = Stack<TerminalNode>()
                Stack<ParserRuleContext>().let { fringe ->
                    fringe.push(ctx)
                    while (!fringe.isEmpty()) {
                        fringe.pop().myLoopingOnChildren(
                            targetTerminalIndex = Java8Lexer.Identifier,
                            onTerminal = { accessSeq.push(it); null },
                            targetProductionIndex = Java8Parser.RULE_packageOrTypeNaåme,
                            onProduction = { fringe.push(it); null },
                            reversed = true
                        )
                    }
                }
                accessSeq.forEach { overrideOf(it, HCode.TYPE_IDENTIFIER, Java8Parser.RULE_expressionName).addReplacing() }
            }
        }
    }*/

    // Creation calls (Constuctor calls).
    override fun exitClassInstanceCreationExpression(ctx: Java8Parser.ClassInstanceCreationExpressionContext?) =
        ctx.myLoopingOnChildren(
            targetTerminalIndex = Java8Lexer.Identifier,
            onTerminal = { HCode.TYPE_IDENTIFIER },
            onAddedExit = false
        )

    override fun exitClassInstanceCreationExpression_lf_primary(ctx: Java8Parser.ClassInstanceCreationExpression_lf_primaryContext?) =
        assignOnFirstIdentifier(ctx, HCode.TYPE_IDENTIFIER)


    override fun exitClassInstanceCreationExpression_lfno_primary(ctx: Java8Parser.ClassInstanceCreationExpression_lfno_primaryContext?) =
        ctx.myLoopingOnChildren(
            targetTerminalIndex = Java8Lexer.Identifier,
            onTerminal = { HCode.TYPE_IDENTIFIER },
            onAddedExit = false
        )

    // +-------------+
    // |  FUNCTIONS  |
    //+--------------+
    override fun exitMethodInvocation(ctx: Java8Parser.MethodInvocationContext?) =
        ctx.myLoopingOnChildren(
            targetProductionIndex = Java8Parser.RULE_methodName,
            onProduction = { HCode.FUNCTION_IDENTIFIER },
            targetTerminalIndex = Java8Lexer.Identifier,
            onTerminal = { HCode.FUNCTION_IDENTIFIER },
            onAddedExit = true
        )

    override fun exitMethodInvocation_lf_primary(ctx: Java8Parser.MethodInvocation_lf_primaryContext?) =
        ctx.myLoopingOnChildren(
            targetTerminalIndex = Java8Lexer.Identifier,
            onTerminal = { HCode.FUNCTION_IDENTIFIER }
        )

    override fun exitMethodInvocation_lfno_primary(ctx: Java8Parser.MethodInvocation_lfno_primaryContext?) =
        ctx.myLoopingOnChildren(
            targetProductionIndex = Java8Parser.RULE_methodName,
            onProduction = { HCode.FUNCTION_IDENTIFIER },
            targetTerminalIndex = Java8Lexer.Identifier,
            onTerminal = { HCode.FUNCTION_IDENTIFIER },
            onAddedExit = true
        )

    // +----------+
    // |  FIELDS  |
    //+-----------+

    // Field Access (Known).
    override fun exitFieldAccess(ctx: Java8Parser.FieldAccessContext?) =
        assignOnFirstIdentifier(ctx, HCode.FIELD_IDENTIFIER)

    override fun exitFieldAccess_lf_primary(ctx: Java8Parser.FieldAccess_lf_primaryContext?) =
        assignOnFirstIdentifier(ctx, HCode.FIELD_IDENTIFIER)

    override fun exitFieldAccess_lfno_primary(ctx: Java8Parser.FieldAccess_lfno_primaryContext?) =
        assignOnFirstIdentifier(ctx, HCode.FIELD_IDENTIFIER)

    // Expression Name (navigates to type of field, invoked always in this context).
    override fun exitExpressionName(ctx: Java8Parser.ExpressionNameContext?) {
        val accessSeq = Stack<TerminalNode>()
        Stack<ParserRuleContext>().let { fringe ->
            fringe.push(ctx)
            while (!fringe.isEmpty()) {
                fringe.pop().myLoopingOnChildren(
                    targetTerminalIndex = Java8Lexer.Identifier,
                    onTerminal = { accessSeq.push(it); null },
                    targetProductionIndex = Java8Parser.RULE_ambiguousName,
                    onProduction = { fringe.push(it); null },
                    reversed = true
                )
            }
        }
        accessSeq.removeLastOrNull()
        accessSeq.forEach {
            overrideOf(
                it,
                HCode.FIELD_IDENTIFIER,
                Java8Parser.RULE_expressionName,
                Java8Parser.ruleNames
            ).addReplacing()
        }
    }

    override fun exitNormalAnnotation(ctx: Java8Parser.NormalAnnotationContext?) =
        ctx.myLoopingOnChildren(
            targetTerminalIndex = Java8Lexer.AT,
            onTerminal = { HCode.ANNOTATION_DECLARATOR },
            onAddedExit = true
        )

    override fun exitMarkerAnnotation(ctx: Java8Parser.MarkerAnnotationContext?) =
        ctx.myLoopingOnChildren(
            targetTerminalIndex = Java8Lexer.AT,
            onTerminal = { HCode.ANNOTATION_DECLARATOR },
            onAddedExit = true
        )

    override fun exitSingleElementAnnotation(ctx: Java8Parser.SingleElementAnnotationContext?) =
        ctx.myLoopingOnChildren(
            targetTerminalIndex = Java8Lexer.AT,
            onTerminal = { HCode.ANNOTATION_DECLARATOR },
            onAddedExit = true
        )

    // Note this is possible thanks to reduced complexity in grammar.
    // Otherwise: match typeName, discriminate on parent's ruleIndex.
    override fun exitAnnotationTypeName(ctx: Java8Parser.AnnotationTypeNameContext?) =
        Stack<ParserRuleContext>().let { fringe ->
            fringe.push(ctx)
            while (!fringe.isEmpty()) {
                fringe.pop().myLoopingOnChildren(
                    targetTerminalIndex = Java8Lexer.Identifier,
                    onTerminal = { HCode.ANNOTATION_DECLARATOR },
                    targetProductionIndex = Java8Parser.RULE_ambiguousName,
                    onProduction = { fringe.push(it); null },
                    reversed = true
                )
            }
        }

}
