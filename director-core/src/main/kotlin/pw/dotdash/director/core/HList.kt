@file:Suppress("NOTHING_TO_INLINE")

package pw.dotdash.director.core

sealed class HList<out A : HList<A>> {

    companion object {
        operator fun <A> invoke(): HNil = HNil

        operator fun <A> invoke(a: A): HCons<A, HNil> = HCons(a, HNil)
    }

    abstract operator fun <B> plus(b: B): HCons<B, A>
}

object HNil : HList<HNil>() {

    override fun <B> plus(b: B): HCons<B, HNil> = HCons(b, this)

    override fun equals(other: Any?): Boolean = this === other

    override fun hashCode(): Int = 0

    override fun toString(): String = "HNil"
}

class HCons<out H, out T : HList<T>>(val head: H, val tail: T) : HList<HCons<H, T>>() {

    override fun <B> plus(b: B): HCons<B, HCons<H, T>> = HCons(b, this)

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is HCons<*, *> -> false
        else -> this.head == other.head && this.tail == other.tail
    }

    override fun hashCode(): Int = 31 * this.head.hashCode() + this.tail.hashCode()

    override fun toString(): String = "HCons($head, $tail)"
}

@JvmName("_1")
inline operator fun <A> HCons<A, HNil>.component1(): A =
    this.head

@JvmName("_2")
inline operator fun <B> HCons<*, HCons<B, HNil>>.component2(): B =
    this.tail.head

@JvmName("_3")
inline operator fun <C> HCons<*, HCons<*, HCons<C, HNil>>>.component3(): C =
    this.tail.tail.head

@JvmName("_4")
inline operator fun <D> HCons<*, HCons<*, HCons<*, HCons<D, HNil>>>>.component4(): D =
    this.tail.tail.tail.head

@JvmName("_5")
inline operator fun <E> HCons<*, HCons<*, HCons<*, HCons<*, HCons<E, HNil>>>>>.component5(): E =
    this.tail.tail.tail.tail.head

@JvmName("_6")
inline operator fun <F> HCons<*, HCons<*, HCons<*, HCons<*, HCons<*, HCons<F, HNil>>>>>>.component6(): F =
    this.tail.tail.tail.tail.tail.head

@JvmName("_7")
inline operator fun <G> HCons<*, HCons<*, HCons<*, HCons<*, HCons<*, HCons<*, HCons<G, HNil>>>>>>>.component7(): G =
    this.tail.tail.tail.tail.tail.tail.head

@JvmName("_8")
inline operator fun <H> HCons<*, HCons<*, HCons<*, HCons<*, HCons<*, HCons<*, HCons<*, HCons<H, HNil>>>>>>>>.component8(): H =
    this.tail.tail.tail.tail.tail.tail.tail.head