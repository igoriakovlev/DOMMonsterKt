import org.w3c.dom.Document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.Element
import kotlinx.browser.document
import kotlinx.browser.window
import kotlin.math.*
import env.DB
import env.DB.Row as dbRow

const val ROWS = 100;
const val TIMEOUT = 0;

external fun setTimeout(handler: () -> Unit, timeout: Int)

@JsFun("(array, index) => array[index]")
external fun takeCellObjectFromArray(array: Dynamic, index: Int): Cell

fun createSlider(db: DB) {
    var body = document.querySelector("body")!!
    var theFirstChild = body.firstChild
    var sliderContainer = document.createElement("div") as HTMLElement
    sliderContainer.style.cssText = "display: flex"
    var slider = document.createElement("input") as HTMLElement
    var text = document.createElement("label") as HTMLElement
    text.innerHTML = "mutations : " + floor(db.mutations(null) * 100.0).toInt() + "%"
    text.id = "ratioval"
    slider.setAttribute("type", "range")
    slider.style.cssText = "margin-bottom: 10px; margin-top: 5px"
    slider.addEventListener("change", { e ->
        db.mutations((e.target as HTMLInputElement).value.toDouble() / 100.0)
        (document.querySelector("#ratioval") as Element).innerHTML = "mutations : " + floor(db.mutations(null) * 100.0).toInt() + "%"
    })
    sliderContainer.appendChild(text)
    sliderContainer.appendChild(slider)
    body.insertBefore(sliderContainer, theFirstChild)
}

fun createTableRows(db: DB): List<Row> {
    val tbody = document.querySelector("tbody") as HTMLElement

    val rows = db.generateData(true).map { dbRow ->
        createRow().also { it.update(dbRow) }
    }

    rows.forEach { row ->
        tbody.appendChild(row.dom.el)
    }

    return rows
}

fun Row.update(db: dbRow) {
    val lastSample = db.lastSample!!

    if (db.dbname !== this.dbname) {
        this.dbname = db.dbname
        this.dom.name.textContent = this.dbname
    }

    if (lastSample.countClassName !== this.countClassName) {
        this.countClassName = lastSample.countClassName
        this.dom.countSpan.className = lastSample.countClassName
    }

    if (lastSample.nbQueries != this.nbQueries) {
        this.nbQueries = lastSample.nbQueries
        this.dom.countSpan.textContent = this.nbQueries.toString()
    }

    val cells = this.cells;

    for (i in 0 until 5) {
        val cell = takeCellObjectFromArray(cells, i)
        val query = lastSample.topFiveQueries!![i]

        if (query.elapsedClassName !== cell.className) {
            cell.className = query.elapsedClassName
            cell.dom.el.className = query.elapsedClassName
        }

        if (query.formatElapsed !== cell.elapsed) {
            cell.elapsed = query.formatElapsed
            cell.dom.elapsed.data = query.formatElapsed
        }

        if (query.query !== cell.query) {
            cell.query = query.query
            cell.dom.popoverContent.textContent = cell.query
        }
    }
}

fun redraw(rows: List<Row>, db: DB, perfMonitor: PerfMonitor) {
    var dbs = db.generateData(true).toTypedArray()
    perfMonitor.startProfile("view update")
    dbs.forEachIndexed { i, dbRow ->
        rows[i].update(dbRow)
    }
    perfMonitor.endProfile("view update")
    setTimeout({ redraw(rows, db, perfMonitor) }, db.timeout)
}

fun main() {
    val db = DB(rows = ROWS, timeout = TIMEOUT)
    createSlider(db)

    val monitor = perfMonitor

    monitor.startFPSMonitor()
    monitor.startMemMonitor()
    monitor.initProfiler("view update")

    val rows = createTableRows(db)

    redraw(rows, db, monitor)
}