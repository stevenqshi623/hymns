package org.christian.suffolk;


import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@RestController
public class HymnController {
    private final String hymnLibraryName = "HymnLibrary";
    private final ClassLoader classLoader = getClass().getClassLoader();
    private XSLFPictureData pd = null;

    @RequestMapping("/bookNames")
    public List<String> getBookNames() throws IOException, URISyntaxException {
        URL dirURL = classLoader.getResource(hymnLibraryName);
        List<String> bookNames = new ArrayList<>();
        if (dirURL.getProtocol().equals("file")) {
            bookNames = Arrays.asList(new File(dirURL.toURI()).list());
        } else {
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<>(); //avoid duplicates in case it is a subdirectory
            String dirName = hymnLibraryName + "/";
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(dirName)) { //filter according to the path
                    String entry = name.substring(dirName.length());
                    int checkSubdir = entry.indexOf("/");
                    if (checkSubdir > 0) {
                        // if it is a subdirectory, we just return the directory name
                        entry = entry.substring(0, checkSubdir);
                        result.add(entry);
                    }
                }
            }
            bookNames = new ArrayList<>(result);
        }
        Collections.sort(bookNames);
        return bookNames;
    }

    @RequestMapping("/hymnNames")
    public List<String> getHymnNames(@RequestParam(value = "bookName") String bookName) throws IOException, URISyntaxException {
        String hymnBookName = hymnLibraryName + "/" + bookName;
        URL dirURL = classLoader.getResource(hymnBookName);
        List<String> hymnNames;
        if (dirURL.getProtocol().equals("file")) {
            hymnNames = Arrays.asList(new File(dirURL.toURI()).list());
        } else {
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<>(); //avoid duplicates in case it is a subdirectory
            String dirName = hymnBookName + "/";
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(dirName)) { //filter according to the path
                    String entry = name.substring(dirName.length());
                    int checkSubdir = entry.indexOf("/");
                    if (checkSubdir > 0) {
                        // if it is a subdirectory, we just return the directory name
                        entry = entry.substring(0, checkSubdir);
                        result.add(entry);
                    }
                }
            }
            hymnNames = new ArrayList<>(result);
        }
        Collections.sort(hymnNames);
        return hymnNames;
    }

    @RequestMapping("/hymn")
    public String getHymn(@RequestParam(value = "bookName") String bookName, @RequestParam(value = "hymnName") String hymnName) throws IOException {
        String fileName = hymnLibraryName + "/" + bookName + "/" + hymnName + "/Hymn.json";
        InputStream in = classLoader.getResourceAsStream(fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    @RequestMapping("/bookNameAndHymnIdToName")
    public List<String> getHymnNameWithBookNameAndHymnId(@RequestParam(value = "bookName") String bookName, @RequestParam(value = "hymnId") String hymnId) throws IOException, URISyntaxException {
        List<String> hymnNames = getHymnNames(bookName);
        String hymnPrefix = hymnId + ' ';
        List<String> result = new ArrayList<>();
        for (String hymnName : hymnNames) {
            if (hymnName.startsWith(hymnPrefix)) {
                result.add(hymnName);
                return result;
            }
        }
        return result;
    }

    @RequestMapping("/getPowerPoint")
    public byte[] getPowerPoint(@RequestParam(value = "bookAndHymnIds") List<String> bookAndHymnIds) throws IOException, URISyntaxException {
        PowerPoint ppt = new PowerPoint();
        for (String bookAndHymnId : bookAndHymnIds) {
            insertHymnIntoPowerPoint(bookAndHymnId, ppt);
        }
        // ppt.outputPowerPoint();
        return ppt.getPowerPoint();
    }

    private void insertHymnIntoPowerPoint(String bookAndHymnId, PowerPoint ppt) throws IOException, URISyntaxException {
        String[] bookNameAndHymnId = bookAndHymnId.split("\\$");
        String hymnName = getHymnNameWithBookNameAndHymnId(bookNameAndHymnId[0], bookNameAndHymnId[1]).get(0);
        String hymnJson = getHymn(bookNameAndHymnId[0], hymnName);
        JSONObject hymnObj = new JSONObject(hymnJson);
        String titleText = hymnObj.getString("title");
        String bodyText = hymnObj.getString("body");
        ppt.insertTitlePage(titleText);
        ppt.insertBodyPages(bodyText);
    }

    @RequestMapping("/getPresentation")
    public List<String> getPresentation(@RequestParam(value = "bookAndHymnIds") List<String> bookAndHymnIds) throws IOException, URISyntaxException {
        StringBuilder sb = new StringBuilder();
        for (String bookAndHymnId : bookAndHymnIds) {
            insertHymnIntoPresentation(bookAndHymnId, sb);
        }
        List<String> result = new ArrayList<>();
        result.add(sb.toString());
        return result;
    }

    private void insertHymnIntoPresentation(String bookAndHymnId, StringBuilder sb) throws IOException, URISyntaxException {
        String[] bookNameAndHymnId = bookAndHymnId.split("\\$");
        String hymnName = getHymnNameWithBookNameAndHymnId(bookNameAndHymnId[0], bookNameAndHymnId[1]).get(0);
        String hymnJson = getHymn(bookNameAndHymnId[0], hymnName);
        JSONObject hymnObj = new JSONObject(hymnJson);
        String titleText = hymnObj.getString("title");
        String bodyText = hymnObj.getString("body");
        sb.append("        <section data-markdown data-background-image='background.jpg' data-background-size='100% 100%'>")
                .append("            <textarea data-template>")
                .append("                # ")
                .append(titleText)
                .append("	        </textarea>")
                .append("        </section>");

        insertHymnBodyIntoPresentation(bodyText, sb);
    }

    private void insertHymnBodyIntoPresentation(String bodyText, StringBuilder sb) {
        String[] paragraphs = bodyText.split("\\n\\n");
        for (String paragraphText : paragraphs) {
            sb.append("        <section data-markdown data-background-image='background.jpg' data-background-size='100% 100%'>")
                    .append("	        <textarea data-template>");
            String[] lines = paragraphText.split("\\n");
            for (String line : lines) {
                sb.append("                ## ").append(line);
            }
            sb.append("	        </textarea>").append("        </section>");
        }
    }
}
