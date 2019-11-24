package org.christian.suffolk;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
//        Path path = file.toPath();
//        boolean isRegularFile = Files.isRegularFile(path);
//        boolean isHidden = Files.isReadable(path);
//        boolean isReadable = Files.isReadable(path);
//        boolean isExecutable = Files.isExecutable(path);
//        boolean isSymbolicLink = Files.isSymbolicLink(path);
//        boolean isDirectory = Files.isDirectory(path);
//        boolean isWritable = Files.isWritable(path);
//
//        System.out.println("isRegularFile: " + isRegularFile);
//        System.out.println("isHidden: " + isHidden);
//        System.out.println("isReadable: " + isReadable);
//        System.out.println("isExecutable: " + isExecutable);
//        System.out.println("isSymbolicLink: " + isSymbolicLink);
//        System.out.println("isDirectory: " + isDirectory);
//        System.out.println("isWritable: " + isWritable);
    }

    @RequestMapping("/hymnNames")
    public List<String> getHymnNames(@RequestParam(value = "bookName") String bookName) {
        List<String> hymnNames = new ArrayList<>();
        File hymnBookFile = new File(classLoader.getResource(hymnLibraryName + "/" + bookName).getFile());
        String[] hymnNameList = hymnBookFile.list();
        for (String hymnName : hymnNameList) {
            hymnNames.add(hymnName);
        }
        return hymnNames;
    }

    @RequestMapping("/hymn")
    public String getHymnsOfBook(@RequestParam(value = "bookName") String bookName, @RequestParam(value = "hymnName") String hymnName) throws Exception{
        String fileName = hymnLibraryName + "/" + bookName + "/" + hymnName + "/Chinese.json";
        String hymnFile = classLoader.getResource(fileName).getFile();
        hymnFile =  java.net.URLDecoder.decode(hymnFile,"utf-8");

        BufferedReader br = new BufferedReader(new FileReader(new File(hymnFile)));
        String st;
        StringBuilder sb = new StringBuilder();
        while ((st = br.readLine()) != null) {
            sb.append(st);
        }
        return sb.toString();
    }
}
