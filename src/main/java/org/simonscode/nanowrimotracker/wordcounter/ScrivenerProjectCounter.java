package org.simonscode.nanowrimotracker.wordcounter;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.IntStream;

public class ScrivenerProjectCounter implements IWordcounter {

    private RichTextFileCounter richTextFileCounter = new RichTextFileCounter();

    @Override
    public String getName() {
        return "Scrivener Project";
    }

    @Override
    public List<String> getFileExtensions() {
        return Collections.singletonList("scriv");
    }

    @Override
    public boolean matches(File path) {
        return path.getName().endsWith(".scriv");
    }

    @Override
    public int getWordcount(File path) {
        throw new NotImplementedException();
    }

    @Override
    public int getWordcount(File path, String secondarySelection) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path);
        NodeList node = doc.getElementsByTagName("BinderItem");
        Optional<Node> folderId = IntStream.range(0, node.getLength())
                .mapToObj(node::item)
                .filter(it -> it.getAttributes().getNamedItem("ID").getTextContent().equals(secondarySelection))
                .findFirst();
        int wordcount = 0;
        if (folderId.isPresent()) {
            ArrayList<String> folders = new ArrayList<>();
            parseBinderItems(folders, folderId.get());
            for (String it : folders) {
                File file = new File(new File(it).getParentFile().getAbsolutePath() + "/Files/Docs/" + it + ".rtf");
                if (file.exists()) {
                    wordcount += richTextFileCounter.getWordcount(file);
                }
            }
        } else {
            throw new FileNotFoundException("Scrivener Folder not found.");
        }
        return wordcount;
    }

    @Override
    public boolean needsSecondarySelection() {
        return true;
    }

    @Override
    public Map<String, String> getSecondarySelectionOptions(File path) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path);
        Node node = doc.getChildNodes().item(0).getChildNodes().item(1);

        Map<String, String> options = new HashMap<>();
        boolean examine = true;
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            // every second item denotes a directory
            examine = !examine;
            Node item = node.getChildNodes().item(i);
            if (examine && item != null) {
                if (item.getAttributes().getNamedItem("Type").getTextContent().contains("Folder")) {
                    options.put(item.getChildNodes().item(1).getTextContent(), item.getAttributes().getNamedItem("ID").getTextContent());
                }
            }
        }
        return options;
    }

    /**
     * Recursivly parses the project file for folders containing textfiles.
     */
    private static void parseBinderItems(List<String> outputList, Node node) {
        Node subNode = null;
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);
            if (item.getNodeName().equals("Children")) {
                subNode = item;
                break;
            }
        }
        if (subNode != null) {
            int bound = subNode.getChildNodes().getLength();
            for (int it = 0; it < bound; it++) {
                Node item = subNode.getChildNodes().item(it);
                if (item.getNodeName().equals("BinderItem") && item.hasAttributes()) {
                    parseBinderItems(outputList, item);
                }
            }
        }
        if (node.getAttributes().getNamedItem("Type").getTextContent().equals("Text")) {
            outputList.add(node.getAttributes().getNamedItem("ID").getTextContent());
        }
    }
}
