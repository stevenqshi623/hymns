package org.christian.suffolk;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@RestController
public class HymnController {
    private final String hymnLibraryName = "HymnLibrary";
    private ClassLoader classLoader = getClass().getClassLoader();

    @RequestMapping("/bookNames")
    public List<String> getBookNames() throws IOException {
        List<String> bookNames = new ArrayList<>();
        File hymnLibraryFile = new File(classLoader.getResource(hymnLibraryName).getFile());
        String[] bookNameList = hymnLibraryFile.list();
        for (String bookName : bookNameList) {
            bookNames.add(bookName);
        }
        return bookNames;
    }

    @RequestMapping("/hymnNames")
    public List<String> getHymnNames(@RequestParam(value = "bookName") String bookName) throws UnsupportedEncodingException {
        String hymnBookName = classLoader.getResource(hymnLibraryName + "/" + bookName).getFile();
        hymnBookName = java.net.URLDecoder.decode(hymnBookName, "utf-8");
        File hymnBookFile = new File(hymnBookName);
        List<String> hymnNames = new ArrayList<>();
        String[] hymnNameList = hymnBookFile.list();
        for (String hymnName : hymnNameList) {
            hymnNames.add(hymnName);
        }
        return hymnNames;
    }

    @RequestMapping("/hymn")
    public String getHymnsOfBook(@RequestParam(value = "bookName") String bookName, @RequestParam(value = "hymnName") String hymnName) throws IOException {
        String fileName = hymnLibraryName + "/" + bookName + "/" + hymnName + "/Chinese.json";
        String hymnFile = classLoader.getResource(fileName).getFile();
        hymnFile = java.net.URLDecoder.decode(hymnFile, "utf-8");
        BufferedReader br = new BufferedReader(new FileReader(new File(hymnFile)));
        String st;
        StringBuilder sb = new StringBuilder();
        while ((st = br.readLine()) != null) {
            sb.append(st);
        }
        return sb.toString();
    }

    @RequestMapping("/bookNameAndHymnIdToName")
    public List<String> getHymnNameWithBookNameAndHymnId(@RequestParam(value = "bookName") String bookName, @RequestParam(value = "hymnId") String hymnId) throws UnsupportedEncodingException {
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
