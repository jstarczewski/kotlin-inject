package me.tatarka.inject.compiler.ast

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import kotlin.reflect.KClass

interface AstProvider {
    fun KClass<*>.toAstClass(): AstClass

    fun declaredTypeOf(astClass: AstClass, vararg astTypes: AstType): AstType

    fun declaredTypeOf(klass: KClass<*>, vararg astTypes: AstType): AstType

    fun warn(message: String, element: AstElement? = null)

    fun error(message: String, element: AstElement?)

    fun TypeSpec.Builder.addOriginatingElement(astClass: AstClass): TypeSpec.Builder
}

sealed class AstElement : AstAnnotated {
    inline fun <reified T : Annotation> annotationOf(): T? = annotationOf(T::class)

    inline fun <reified T : Annotation> typeAnnotatedWith(): AstClass? = typeAnnotatedWith(T::class)
}

interface AstAnnotated {
    fun <T : Annotation> annotationOf(klass: KClass<T>): T?

    fun <T : Annotation> typeAnnotatedWith(kclass: KClass<T>): AstClass?
}

abstract class AstBasicElement : AstElement() {
    abstract val simpleName: String
}

abstract class AstClass : AstElement() {

    abstract val packageName: String

    abstract val name: String

    abstract val modifiers: Set<AstModifier>

    abstract val companion: AstClass?

    abstract val superclass: AstClass?

    abstract val interfaces: List<AstClass>

    abstract val constructors: List<AstConstructor>

    abstract val methods: List<AstMethod>

    abstract val type: AstType

    fun visitInheritanceChain(f: (AstClass) -> Unit) {
        f(this)
        superclass?.visitInheritanceChain(f)
        interfaces.forEach { it.visitInheritanceChain(f) }
    }

    abstract fun asClassName(): ClassName

    override fun toString(): String {
        return if (packageName.isEmpty() || packageName == "kotlin") name else "$packageName.$name"
    }
}

sealed class AstMethod : AstElement() {
    abstract val name: String

    abstract val modifiers: Set<AstModifier>

    abstract val receiverParameterType: AstType?

    abstract val returnType: AstType

    abstract fun returnTypeFor(enclosingClass: AstClass): AstType
}

abstract class AstConstructor(private val parent: AstClass) : AstElement() {
    val type: AstType get() = parent.type

    abstract val parameters: List<AstParam>
}

abstract class AstFunction : AstMethod() {
    abstract val parameters: List<AstParam>
}

abstract class AstProperty : AstMethod()

abstract class AstType : AstElement() {

    abstract val name: String

    abstract val annotations: List<AstAnnotation>

    abstract val abbreviatedTypeName: String?

    abstract val arguments: List<AstType>

    abstract fun isUnit(): Boolean

    @Suppress("NOTHING_TO_INLINE")
    inline fun isNotUnit() = !isUnit()

    abstract fun asElement(): AstBasicElement

    abstract fun toAstClass(): AstClass

    abstract fun asTypeName(): TypeName

    override fun equals(other: Any?): Boolean {
        if (other !is AstType) return false
        return asTypeName() == other.asTypeName()
    }

    override fun hashCode(): Int {
        return asTypeName().hashCode()
    }

    override fun toString(): String {
        val n = name
        return if (n.substringBeforeLast('.') == "kotlin") n.removePrefix("kotlin.") else n
    }
}

abstract class AstAnnotation : AstElement()

abstract class AstParam : AstElement() {

    abstract val name: String

    abstract val type: AstType

    abstract fun asParameterSpec(): ParameterSpec
}

enum class AstModifier {
    PRIVATE, ABSTRACT
}

fun ParameterSpec.Companion.parametersOf(constructor: AstConstructor) =
    constructor.parameters.map { it.asParameterSpec() }
