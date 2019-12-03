package org.christian.suffolk;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${hymn.library.path}")
    private String hymnLibraryPath;

    private class LyricData {
        String lyric;
        Boolean hasChineseVersion;
        Boolean hasEnglishVersion;

        public LyricData(String lyric, Boolean hasChineseVersion, Boolean hasEnglishVersion) {
            this.lyric = lyric;
            this.hasChineseVersion = hasChineseVersion;
            this.hasEnglishVersion = hasEnglishVersion;
        }

        public String getLyric() {
            return lyric;
        }

        public Boolean getHasChineseVersion() {
            return hasChineseVersion;
        }

        public Boolean getHasEnglishVersion() {
            return hasEnglishVersion;
        }
    }

    @RequestMapping("/getBookNameToAllHymns")
    public Map<String,Set<String>> getBookNameToAllHymns() throws IOException, URISyntaxException {
        File hymnLibraryDir = new File(hymnLibraryPath);
        Map<String, Set<String>> results = new TreeMap<>();
        String[] bookNames = hymnLibraryDir.list();
        if (bookNames != null) {
            for (String bookName : bookNames) {
                if (bookName.startsWith(".")) {
                    continue;
                }
                String hymnBookPath = hymnLibraryPath + "/" + bookName;
                File hymnBookDir = new File(hymnBookPath);
                Set<String> treeSet = new TreeSet<>((s1, s2) -> {
                    int i1 = Integer.parseInt(s1.substring(0, s1.indexOf('$')));
                    int i2 = Integer.parseInt(s2.substring(0, s2.indexOf('$')));
                    return i1 - i2;
                });
                treeSet.addAll(Arrays.asList(hymnBookDir.list()));
                results.put(bookName, treeSet);
            }
        }
        return results;
    }

    @RequestMapping("/getLyrics")
    public List<String> getLyrics(@RequestParam(value = "bookNameAndHymnNames") List<String> bookNameAndHymnNames,
                                  @RequestParam(value = "includeChineseVersion") List<Boolean> includeChineseVersion,
                                  @RequestParam(value = "includeEnglishVersion") List<Boolean> includeEnglishVersion)
            throws IOException, URISyntaxException {
        List<String> result = new ArrayList<>();
        List<LyricData> allLyricData = getAllLyricData(bookNameAndHymnNames, includeChineseVersion, includeEnglishVersion);
        for (LyricData lyricData : allLyricData) {
            result.add(lyricData.getLyric().replaceFirst("\\n\\n", "\n\n\n").replaceAll("\\n\\n", "\n"));
        }
        return result;
    }

    @RequestMapping("/showSlides")
    public List<String> showSlides(@RequestParam(value = "bookNameAndHymnNames") List<String> bookNameAndHymnNames,
                                   @RequestParam(value = "includeChineseVersion") List<Boolean> includeChineseVersion,
                                   @RequestParam(value = "includeEnglishVersion") List<Boolean> includeEnglishVersion)
            throws IOException {
        List<LyricData> allLyricData = getAllLyricData(bookNameAndHymnNames, includeChineseVersion, includeEnglishVersion);
        StringBuilder sb = new StringBuilder();
        for (LyricData lyricData : allLyricData) {
            insertHymnIntoWebSlides(lyricData, sb);
        }
        List<String> result = new ArrayList<>();
        result.add(sb.toString());
        return result;
    }

    @RequestMapping("/downloadSlides")
    public byte[] downloadSlides(@RequestParam(value = "bookNameAndHymnNames") List<String> bookNameAndHymnNames,
                                 @RequestParam(value = "includeChineseVersion") List<Boolean> includeChineseVersion,
                                 @RequestParam(value = "includeEnglishVersion") List<Boolean> includeEnglishVersion)
            throws IOException {
        List<LyricData> allLyricData = getAllLyricData(bookNameAndHymnNames, includeChineseVersion, includeEnglishVersion);
        PowerPoint ppt = new PowerPoint();
        for (LyricData lyricData : allLyricData) {
            insertHymnIntoMsSlides(lyricData, ppt);
        }
        // ppt.outputPowerPoint();
        return ppt.getPowerPoint();
    }

    private List<LyricData> getAllLyricData(List<String> bookNameAndHymnNames, List<Boolean> includeChineseVersion, List<Boolean> includeEnglishVersion) throws IOException {
        List<LyricData> result = new ArrayList<>();
        for (int i = 0; i < bookNameAndHymnNames.size(); i++) {
            result.add(getLyricData(bookNameAndHymnNames.get(i), includeChineseVersion.get(i), includeEnglishVersion.get(i)));
        }
        return result;
    }

    private LyricData getLyricData(String bookNameAndHymnNameStr,
                          Boolean includeChineseVersion,
                          Boolean includeEnglishVersion) throws IOException {
        int bookNameEndIndex = bookNameAndHymnNameStr.indexOf('$');
        String bookName = bookNameAndHymnNameStr.substring(0, bookNameEndIndex);

        String hymnName = bookNameAndHymnNameStr.substring(bookNameEndIndex + 1);
        String[] hymnInfo = hymnName.split("\\$");
        JSONObject chineseObj = null;
        JSONObject englishObj = null;
        if (includeChineseVersion && !hymnInfo[1].isEmpty()) {
            chineseObj = readLyric(hymnLibraryPath + "/" + bookName + "/" + hymnName + "/Chinese.json");
        }
        if (includeEnglishVersion && hymnInfo.length == 3 && !hymnInfo[2].isEmpty()) {
            englishObj = readLyric(hymnLibraryPath + "/" + bookName + "/" + hymnName + "/English.json");
        }
        return merge(chineseObj, englishObj);
    }

    private LyricData merge(JSONObject chineseObj, JSONObject englishObj) {
        StringBuilder sb = new StringBuilder();
        if (chineseObj != null && englishObj != null) {
            sb.append(chineseObj.get("title"))
                    .append("\n")
                    .append(englishObj.get("title"))
                    .append("\n\n");
            String[] chineseParagraphs = ((String)chineseObj.get("body")).split("\\n\\n");
            String[] englishParagraphs = ((String)englishObj.get("body")).split("\\n\\n");
            for (int i = 0; i < chineseParagraphs.length; i++) {
                sb.append(chineseParagraphs[i]).append("\n\n").append(englishParagraphs[i]).append("\n\n");
            }
            sb.setLength(sb.length() - 2);
        }
        else if (chineseObj != null) {
            sb.append(chineseObj.get("title")).append("\n\n").append(chineseObj.get("body"));
        }
        else if (englishObj != null) {
            sb.append(englishObj.get("title")).append("\n\n").append(englishObj.get("body"));
        }
        return new LyricData(sb.toString(), chineseObj != null, englishObj != null);
    }

    private JSONObject readLyric(String path) throws IOException {
        InputStream in = new FileInputStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return new JSONObject(sb.toString());
    }

    private void insertHymnIntoWebSlides(LyricData lyricData, StringBuilder sb) {
        String[] texts = lyricData.getLyric().split("\\n\\n");
        String breakStr = (lyricData.getHasChineseVersion() && lyricData.getHasEnglishVersion()) ? "<br>" : "";
        String[] titles = texts[0].split("\\n");
        sb.append("<section data-background-image='background.jpg' data-background-size='100% 100%'>");
        for (String title : titles) {
            sb.append("<h1>").append(title).append("</h1>");
        }
        sb.append("</section>");
        for (int i = 1; i < texts.length; i += 2) {
            sb.append("<section data-background-image='background.jpg' data-background-size='100% 100%'>");
            insertParagraphIntoWebSlides(texts[i], sb);
            if (i + 1 < texts.length) {
                sb.append(breakStr);
                insertParagraphIntoWebSlides(texts[i + 1], sb);
            }
            sb.append("</section>");
        }
    }

    private void insertParagraphIntoWebSlides(String text, StringBuilder sb) {
        String[] lines = text.split("\\n");
        for (String line : lines) {
            sb.append("<h2>").append(line).append("</h2>");
        }
    }

    private void insertHymnIntoMsSlides(LyricData lyricData, PowerPoint ppt){
        String[] texts = lyricData.getLyric().split("\\n\\n");
        ppt.insertTitlePage(texts[0]);
        ppt.insertBodyPages(texts, lyricData.getHasChineseVersion() && lyricData.getHasEnglishVersion());
    }
}
