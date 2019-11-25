package org.christian.suffolk;


import org.springframework.web.bind.annotation.CrossOrigin;
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
    private ClassLoader classLoader = getClass().getClassLoader();

    @RequestMapping("/bookNames")
    public List<String> getBookNames() throws IOException, URISyntaxException {
        URL dirURL = classLoader.getResource(hymnLibraryName);
        List<String> bookNames = new ArrayList<>();
        if (dirURL.getProtocol().equals("file")) {
            bookNames = Arrays.asList(new File(dirURL.toURI()).list());
        }
        else {
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<>(); //avoid duplicates in case it is a subdirectory
            String dirName = hymnLibraryName + "/";
            while(entries.hasMoreElements()) {
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
        List<String> hymnNames = new ArrayList<>();
        if (dirURL.getProtocol().equals("file")) {
            hymnNames = Arrays.asList(new File(dirURL.toURI()).list());
        }
        else {
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<>(); //avoid duplicates in case it is a subdirectory
            String dirName = hymnBookName + "/";
            while(entries.hasMoreElements()) {
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
        System.out.println("fileName: " + fileName);
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
}
