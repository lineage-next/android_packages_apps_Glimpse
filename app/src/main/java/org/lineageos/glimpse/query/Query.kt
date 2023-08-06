package org.lineageos.glimpse.query

inline fun buildQuery(builderAction: Query.() -> Unit): Query {
    return Query().apply(builderAction)
}

open class Query {
    private lateinit var root: Node

    fun or(lhs: Query, rhs: Query): Query {
        root = Or(lhs.root, rhs.root)
        return this
    }

    fun and(lhs: Query, rhs: Query): Query {
        root = And(lhs.root, rhs.root)
        return this
    }

    fun <T> eq(col: String, `val`: T): Query {
        root = Eq(col, `val`)
        return this
    }

    fun build() = if (valid()) root.build() else ""
    fun valid() = this::root.isInitialized

    interface Node {
        fun build(): String = when (this) {
            is Eq<*> -> "$col = $`val`"
            is Or -> "(${lhs.build()}) OR (${rhs.build()})"
            is And -> "(${lhs.build()}) AND (${rhs.build()})"
            else -> ""
        }
    }

    private data class Eq<T>(val col: String, val `val`: T) : Node
    private data class Or(val lhs: Node, val rhs: Node) : Node
    private data class And(val lhs: Node, val rhs: Node) : Node
}


object test {
    fun ll() {
        buildQuery {
            or(
                eq("ciao", ""),
                or(
                    eq("test", 1),
                    and(
                        eq("", ""), eq("", "")
                    )
                )
            )
        }
    }
}