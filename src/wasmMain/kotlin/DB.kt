package env

import kotlin.math.*
import kotlin.random.Random

class DB(private val rows: Int, val timeout: Int) {
    class Row(var dbname: String = "", var lastMutationId: Int = 0, var lastSample: Sample? = null, var nbQueries: Int = 0)
    class Sample(var countClassName: String = "", var nbQueries: Int = 0, var queries: MutableList<Query>? = null, var topFiveQueries: MutableList<Query>? = null)
    class Query(var elapsed: Double? = null, var formatElapsed: String = "", var elapsedClassName: String = "", var query: String = "", var waiting: Boolean? = null)

    private var first = true
    private var counter = 0
    private var data: MutableList<Row>? = null

    fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

    fun formatElapsed(value: Double): String {
        var str = value.round(2).toString()
        if (value > 60) {
            val minutes = floor(value / 60)
            val comps = (value % 60).toString().split('.')
            val seconds = comps[0].padStart(2, '0')
            val ms = comps[1]
            str = "$minutes:$seconds.$ms"
        }
        return str
    }

    fun getElapsedClassName(elapsed: Double): String {
        var className = "uery elapsed"
        if (elapsed >= 10.0) {
            className += " warn_long"
        }
        else if (elapsed >= 1.0) {
            className += " warn"
        }
        else {
            className += " short"
        }
        return className
    }

    fun countClassName(queries: Int): String {
        var countClassName = "label"
        if (queries >= 20) {
            countClassName += " label-important"
        }
        else if (queries >= 10) {
            countClassName += " label-warning"
        }
        else {
            countClassName += " label-success"
        }
        return countClassName
    }

    fun updateQuery(query: Query?): Query {
        val obj = query ?: Query()
        var elapsed = Random.nextDouble() * 15.0
        obj.elapsed = elapsed
        obj.formatElapsed = formatElapsed(elapsed)
        obj.elapsedClassName = getElapsedClassName(elapsed)
        obj.query = "SELECT blah FROM something"
        obj.waiting = Random.nextDouble() < 0.5
        if (Random.nextDouble() < 0.2) {
            obj.query = "<IDLE> in transaction"
        }
        if (Random.nextDouble() < 0.1) {
            obj.query = "vacuum"
        }
        return obj
    }

    fun cleanQuery(value: Query?): Query {
        if (value != null) {
            value.formatElapsed = ""
            value.elapsedClassName = ""
            value.query = ""
            value.elapsed = null
            value.waiting = null
        } else {
            return Query(query = "***")
        }
        return value
    }

    fun generateRow(row: Row?, keepIdentity: Boolean, counter: Int): Row {
        val nbQueries = floor((Random.nextDouble() * 10.0) + 1.0).toInt()

        val obj = row ?: Row()

        obj.lastMutationId = counter
        obj.nbQueries = nbQueries
        val lastSample = obj.lastSample ?: Sample()
        obj.lastSample = lastSample
        val queries: List<Query>

        if (keepIdentity) {
            val current = lastSample.queries
            if (current == null) {
                val newQueries = mutableListOf<Query>()
                lastSample.queries = newQueries
                for (i in 0 until 12) {
                    newQueries.add(cleanQuery(null))
                }
                queries = newQueries
            } else {
                current.forEachIndexed { j, value ->
                    if (j <= nbQueries) {
                        updateQuery(value)
                    } else {
                        cleanQuery(value)
                    }
                }
                queries = current
            }
        } else {
            queries = mutableListOf()
            lastSample.queries = queries
            for (j in 0 until 12) {
                if (j < nbQueries) {
                    val value = updateQuery(cleanQuery(null))
                    queries.add(value)
                } else {
                    queries.add(cleanQuery(null))
                }
            }
        }

        val topFiveQueries = lastSample.topFiveQueries
        if (topFiveQueries == null) {
            val newTopFiveQueries = mutableListOf<Query>()
            for (i in 0 until 5) {
                newTopFiveQueries.add(queries[i])
            }
            lastSample.topFiveQueries = newTopFiveQueries
        } else {
            for (i in 0 until 5) {
                topFiveQueries[i] = queries[i]
            }
        }
        lastSample.nbQueries = nbQueries
        lastSample.countClassName = countClassName(nbQueries)
        return obj
    }

    fun generateData(keepIdentity: Boolean): List<Row> {
        var oldData = data
        if (!keepIdentity) { // reset for each tick when !keepIdentity
            val newData = mutableListOf<Row>()
            data = newData
            for (i in 1..rows) {
                newData.add(Row(dbname = "cluster$i"))
                newData.add(Row(dbname = "cluster$i slave"))
            }
        }
        if (data == null) { // first init when keepIdentity
            val newData = mutableListOf<Row>()
            data = newData
            for (i in 1..rows) {
                newData.add(Row(dbname = "cluster$i"))
                newData.add(Row(dbname = "cluster$i slave"))
            }
            oldData = data
        }

        val data = data!!

        for (i in data.indices) {
            val row = data[i]
            if (!keepIdentity && oldData != null && oldData.size < i) {
                row.lastSample = oldData[i].lastSample
            }
            if (row.lastSample == null || Random.nextDouble() < mutations(null)) {
                counter++
                if (!keepIdentity) {
                    row.lastSample = null
                }
                generateRow(row, keepIdentity, counter)
            } else {
                data[i] = oldData!![i]
            }
        }
        first = false
        return data
    }

    var mutationsValue = 0.5

    fun mutations(value: Double?): Double {
        if (value != null) {
            mutationsValue = value
            return mutationsValue
        } else {
            return mutationsValue
        }
    }
}