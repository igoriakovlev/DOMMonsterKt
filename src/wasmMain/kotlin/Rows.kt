import org.w3c.dom.Document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.Element
import org.w3c.dom.Text
import kotlinx.browser.document
import kotlinx.browser.window

external class RowDom {
    var el: HTMLElement
    var name: HTMLElement
    var count: HTMLElement
    var countSpan: HTMLElement
}

external class Row {
    var dom: RowDom
    var cells: Dynamic
    var dbname: String
    var countClassName: String
    var nbQueries: Int
}

external class CellDom {
    var el: HTMLElement
    var elapsed: Text
    var popover: HTMLElement
    var popoverContent: HTMLElement
    var popoverArrow: HTMLElement
}

external class Cell {
    var dom: CellDom
    var className: String
    var elapsed: String
    var query: String
}

@JsFun("() => ({})")
private external fun createEmptyRowObject(): Row

@JsFun("() => ({})")
private external fun createEmptyRowDomObject(): RowDom

@JsFun("() => ({})")
private external fun createEmptyCellObject(): Cell

@JsFun("() => ({})")
private external fun createEmptyCellDomObject(): CellDom

@JsFun("() => ([])")
private external fun createEmptyArrayObject(): Dynamic

@JsFun("(array, cell) => array.push(cell)")
private external fun pushCellObjectToArray(array: Dynamic, cell: Cell)

fun createRow(): Row {
    val rowDom = createEmptyRowDomObject()
    rowDom.el = document.createElement("tr") as HTMLElement

    rowDom.name = rowDom.el.appendChild(document.createElement("td")) as HTMLElement
    rowDom.name.className = "dbname"

    rowDom.count = rowDom.el.appendChild(document.createElement("td")) as HTMLElement
    rowDom.count.className = "query-count"

    rowDom.countSpan = rowDom.count.appendChild(document.createElement("span")  as HTMLElement) as HTMLElement;

    val rowDomCells = createEmptyArrayObject()
    for (i in 0 until 5) {
        val cellDom = createEmptyCellDomObject()
        cellDom.el = rowDom.el.appendChild(document.createElement("td")) as HTMLElement

        cellDom.elapsed = cellDom.el.appendChild(document.createTextNode("")) as Text

        cellDom.popover = cellDom.el.appendChild(document.createElement("div")) as HTMLElement
        cellDom.popover.className = "popover left"

        cellDom.popoverContent = cellDom.popover.appendChild(document.createElement("div")) as HTMLElement
        cellDom.popoverContent.className = "popover-content"

        cellDom.popoverArrow = cellDom.popover.appendChild(document.createElement("div")) as HTMLElement
        cellDom.popoverArrow.className = "arrow"

        val cell = createEmptyCellObject()
        cell.dom = cellDom
        cell.className = ""
        cell.elapsed = ""
        cell.query = ""
        pushCellObjectToArray(rowDomCells, cell)
    }

    val row = createEmptyRowObject()
    row.dom = rowDom
    row.cells = rowDomCells
    row.dbname = ""
    row.countClassName = ""
    return row
}