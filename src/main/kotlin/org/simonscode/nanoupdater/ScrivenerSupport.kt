package org.simonscode.nanoupdater

import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

object ScrivenerSupport {
    fun getScrivenerFolders(file: File): MutableMap<String, String> {
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
        return list
    }

    fun getWordcount(): Int {
        val file = File(Config.get().doumentPath)
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val node = doc.getElementsByTagName("BinderItem")
        val wordcount =
                (0..node.length)
                        .asSequence()
                        .mapNotNull { node.item(it) }
                        .filter { it.attributes.getNamedItem("ID").textContent == Config.get().scrivenerFolder }
                        .firstOrNull()
                        ?.let {
                            val list = ArrayList<String>()
                            parse(list, it)
                            list
                        }
                        ?.map { File(file.parentFile.absolutePath + "/Files/Docs/$it.rtf") }
                        ?.filter { it.exists() }
                        ?.map { NanoUpdater.getWordcount(it) }
                        ?.sum()

        return if (wordcount != null) {
            wordcount
        } else {
            Config.get().currentVersion = "0.0"
            Config.get().save()
            LogWindow.log("\n\nERROR! Could not find requested Scrivener folder!\n\n")
            -1
        }
    }

    private fun parse(outputList: MutableList<String>, node: Node) {
        val childrenSubNode =
                (0..node.childNodes.length)
                        .asSequence()
                        .mapNotNull { node.childNodes.item(it) }
                        .filter { it.nodeName == "Children" }
                        .firstOrNull()
        if (childrenSubNode != null) {
            (0..childrenSubNode.childNodes.length)
                    .asSequence()
                    .mapNotNull { childrenSubNode.childNodes.item(it) }
                    .filter { it.nodeName == "BinderItem" && it.hasAttributes() }
                    .forEach { parse(outputList, it) }
        }
        if (node.attributes.getNamedItem("Type").textContent == "Text") {
            outputList.add(node.attributes.getNamedItem("ID").textContent)
        }
    }
}