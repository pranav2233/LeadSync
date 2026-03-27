package com.example.leadsync.export

import com.example.leadsync.data.MeetingRecord
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object InteractionWorkbookExporter {
    private val isoDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun export(personName: String, meetings: List<MeetingRecord>): ByteArray {
        val rows = buildRows(meetings)
        val output = ByteArrayOutputStream()

        ZipOutputStream(output).use { zip ->
            zip.writeEntry("[Content_Types].xml", contentTypesXml)
            zip.writeEntry("_rels/.rels", rootRelationshipsXml)
            zip.writeEntry("docProps/app.xml", appPropertiesXml)
            zip.writeEntry("docProps/core.xml", corePropertiesXml)
            zip.writeEntry("xl/workbook.xml", workbookXml(sanitizeSheetName(personName)))
            zip.writeEntry("xl/_rels/workbook.xml.rels", workbookRelationshipsXml)
            zip.writeEntry("xl/styles.xml", stylesXml)
            zip.writeEntry("xl/worksheets/sheet1.xml", worksheetXml(rows))
        }

        return output.toByteArray()
    }

    private fun buildRows(meetings: List<MeetingRecord>): List<List<String>> {
        return buildList {
            add(
                listOf(
                    "Date",
                    "Interaction Type",
                    "Agenda",
                    "Progress Summary",
                    "Feedback",
                    "Action Items",
                ),
            )
            meetings.forEach { meeting ->
                add(
                    listOf(
                        formatDate(meeting.scheduledAt),
                        meeting.interactionType,
                        meeting.agenda,
                        meeting.progressSummary,
                        meeting.feedback,
                        formatActionItems(meeting),
                    ),
                )
            }
        }
    }

    private fun worksheetXml(rows: List<List<String>>): String {
        val rowXml = rows.mapIndexed { rowIndex, row ->
            val cells = row.mapIndexed { columnIndex, value ->
                val reference = "${columnName(columnIndex + 1)}${rowIndex + 1}"
                val escapedValue = escapeXml(sanitizeCellText(value))
                """<c r="$reference" t="inlineStr"><is><t xml:space="preserve">$escapedValue</t></is></c>"""
            }.joinToString(separator = "")
            """<row r="${rowIndex + 1}">$cells</row>"""
        }.joinToString(separator = "")

        val lastColumn = columnName(rows.maxOfOrNull { it.size } ?: 1)
        val lastRow = rows.size.coerceAtLeast(1)

        return """
            <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
              <dimension ref="A1:$lastColumn$lastRow"/>
              <sheetViews>
                <sheetView workbookViewId="0"/>
              </sheetViews>
              <sheetFormatPr defaultRowHeight="15"/>
              <sheetData>$rowXml</sheetData>
              <pageMargins left="0.7" right="0.7" top="0.75" bottom="0.75" header="0.3" footer="0.3"/>
            </worksheet>
        """.trimIndent()
    }

    private fun formatDate(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(isoDateFormatter)
    }

    private fun formatActionItems(meeting: MeetingRecord): String {
        if (meeting.actionItems.isEmpty()) return ""
        return meeting.actionItems.joinToString(separator = "\n") { item ->
            buildString {
                append(item.title)
                if (item.owner.isNotBlank()) {
                    append(" | owner: ")
                    append(item.owner)
                }
                item.dueAt?.let {
                    append(" | due: ")
                    append(formatDate(it))
                }
                append(" | status: ")
                append(item.status.label)
                if (item.notes.isNotBlank()) {
                    append(" | notes: ")
                    append(item.notes)
                }
            }
        }
    }

    private fun sanitizeSheetName(name: String): String {
        val cleaned = name
            .replace(Regex("""[\\/*?:\[\]]"""), " ")
            .trim()
            .ifBlank { "Interactions" }
        return cleaned.take(31)
    }

    private fun sanitizeCellText(value: String): String {
        val normalized = value.replace("\r\n", "\n").replace('\r', '\n')
        val builder = StringBuilder(normalized.length)
        var index = 0
        while (index < normalized.length) {
            val codePoint = normalized.codePointAt(index)
            if (isValidXmlCodePoint(codePoint)) {
                builder.appendCodePoint(codePoint)
            }
            index += Character.charCount(codePoint)
        }
        return builder.toString()
    }

    private fun isValidXmlCodePoint(codePoint: Int): Boolean {
        return codePoint == 0x9 ||
            codePoint == 0xA ||
            codePoint == 0xD ||
            codePoint in 0x20..0xD7FF ||
            codePoint in 0xE000..0xFFFD ||
            codePoint in 0x10000..0x10FFFF
    }

    private fun columnName(index: Int): String {
        var value = index
        val result = StringBuilder()
        while (value > 0) {
            val remainder = (value - 1) % 26
            result.append(('A'.code + remainder).toChar())
            value = (value - 1) / 26
        }
        return result.reverse().toString()
    }

    private fun escapeXml(value: String): String {
        return buildString(value.length) {
            value.forEach { char ->
                when (char) {
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    '"' -> append("&quot;")
                    '\'' -> append("&apos;")
                    else -> append(char)
                }
            }
        }
    }

    private fun ZipOutputStream.writeEntry(
        name: String,
        content: String,
    ) {
        putNextEntry(ZipEntry(name))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private val contentTypesXml = """
        <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
          <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
          <Default Extension="xml" ContentType="application/xml"/>
          <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
          <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
          <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
          <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
          <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
        </Types>
    """.trimIndent()

    private val rootRelationshipsXml = """
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
          <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
          <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
          <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
        </Relationships>
    """.trimIndent()

    private val appPropertiesXml = """
        <Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
          <Application>LeadSync</Application>
        </Properties>
    """.trimIndent()

    private val corePropertiesXml = """
        <cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
          <dc:creator>LeadSync</dc:creator>
          <cp:lastModifiedBy>LeadSync</cp:lastModifiedBy>
          <dcterms:created xsi:type="dcterms:W3CDTF">2026-03-27T00:00:00Z</dcterms:created>
          <dcterms:modified xsi:type="dcterms:W3CDTF">2026-03-27T00:00:00Z</dcterms:modified>
        </cp:coreProperties>
    """.trimIndent()

    private fun workbookXml(sheetName: String): String {
        return """
            <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
              <bookViews>
                <workbookView xWindow="0" yWindow="0" windowWidth="28800" windowHeight="14400"/>
              </bookViews>
              <sheets>
                <sheet name="${escapeXml(sheetName)}" sheetId="1" r:id="rId1"/>
              </sheets>
            </workbook>
        """.trimIndent()
    }

    private val workbookRelationshipsXml = """
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
          <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
          <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
        </Relationships>
    """.trimIndent()

    private val stylesXml = """
        <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
          <numFmts count="0"/>
          <fonts count="1">
            <font>
              <sz val="11"/>
              <name val="Calibri"/>
              <family val="2"/>
            </font>
          </fonts>
          <fills count="2">
            <fill><patternFill patternType="none"/></fill>
            <fill><patternFill patternType="gray125"/></fill>
          </fills>
          <borders count="1">
            <border>
              <left/>
              <right/>
              <top/>
              <bottom/>
              <diagonal/>
            </border>
          </borders>
          <cellStyleXfs count="1">
            <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
          </cellStyleXfs>
          <cellXfs count="1">
            <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
          </cellXfs>
          <cellStyles count="1">
            <cellStyle name="Normal" xfId="0" builtinId="0"/>
          </cellStyles>
          <dxfs count="0"/>
          <tableStyles count="0" defaultTableStyle="TableStyleMedium9" defaultPivotStyle="PivotStyleLight16"/>
        </styleSheet>
    """.trimIndent()
}
