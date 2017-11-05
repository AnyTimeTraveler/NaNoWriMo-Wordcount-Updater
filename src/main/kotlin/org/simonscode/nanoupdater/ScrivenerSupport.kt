package org.simonscode.nanoupdater

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

object ScrivenerSupport {
    private fun getScrivenerFolders(file: File): String {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val node = doc.childNodes.item(0).childNodes.item(1)

        val list = mutableMapOf<String, String>()
        var examine = true
        for (i in 0..node.childNodes.length) {
            examine = !examine
            if (examine) {
                node.childNodes.item(i)?.let {
                    if (it.attributes.getNamedItem("Type").textContent.contains("Folder"))
                        list.put(it.childNodes.item(1).textContent, it.attributes.getNamedItem("ID").textContent)
                }
            }
        }
        list.forEach { println(it.key + " : " + it.value) }
        return ""
    }
}