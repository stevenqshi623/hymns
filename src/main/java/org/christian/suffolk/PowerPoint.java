package org.christian.suffolk;

import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.apache.poi.util.IOUtils.toByteArray;

public class PowerPoint {
    private static final ClassLoader classLoader = PowerPoint.class.getClassLoader();
    private static final InputStream in = classLoader.getResourceAsStream("static/background.jpg");
    private static byte[] picture = null;

    static {
        try {
            picture = toByteArray(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private XMLSlideShow ppt = new XMLSlideShow();
    private XSLFSlideMaster defaultMaster = ppt.getSlideMasters().get(0);
    private XSLFSlideLayout titleLayout = defaultMaster.getLayout(SlideLayout.TITLE);
    private XSLFSlideLayout titleAndContentLayout = defaultMaster.getLayout(SlideLayout.TITLE_AND_CONTENT);

    public PowerPoint() {
        XSLFPictureData pd = ppt.addPicture(picture, PictureData.PictureType.JPEG);
        ppt.setPageSize(pd.getImageDimension());
        titleLayout.createPicture(pd);
        titleAndContentLayout.createPicture(pd);
    }

    public void insertTitlePage(String titleText) {
        XSLFSlide slide = ppt.createSlide(titleLayout);
        slide.getPlaceholder(1).clearText();
        XSLFTextShape shape = slide.getPlaceholder(0);
        shape.clearText();

        XSLFTextParagraph paragraph = shape.addNewTextParagraph();
        paragraph.setTextAlign(TextParagraph.TextAlign.CENTER);
        paragraph.setBullet(false);
        paragraph.setIndent(0.0);

        String[] lines = titleText.split("\\n");
        for (String line : lines) {
            XSLFTextRun run = paragraph.addNewTextRun();
            run.setText(line);
            run.setFontSize(54.0);
            run.setBold(true);
            paragraph.addLineBreak();
        }
    }

    public void insertBodyPages(String[] texts, boolean twoVersions) {
        for (int i = 1; i < texts.length; i += 2) {
            XSLFSlide slide = ppt.createSlide(titleAndContentLayout);
            slide.getPlaceholder(0).clearText();

            XSLFTextShape shape = slide.getPlaceholder(1);
            shape.clearText();

            XSLFTextParagraph paragraph = shape.addNewTextParagraph();
            paragraph.setTextAlign(TextParagraph.TextAlign.CENTER);
            paragraph.setBullet(false);
            paragraph.setIndent(0.0);
            paragraph.setLineSpacing(126.0);

            insertParagraphIntoMsSlides(texts[i], paragraph);
            if (i + 1 < texts.length) {
                if (twoVersions) {
                    paragraph.addLineBreak();
                }
                insertParagraphIntoMsSlides(texts[i + 1], paragraph);
            }
        }
    }

    public byte[] getPowerPoint() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ppt.write(baos);
        return baos.toByteArray();
    }

    public void outputPowerPoint() throws IOException {
        FileOutputStream out = new FileOutputStream("powerpoint" + System.currentTimeMillis() + ".pptx");
        ppt.write(out);
        out.close();
    }

    private void insertParagraphIntoMsSlides(String text, XSLFTextParagraph paragraph) {
        String[] lines = text.split("\\n");
        for (String line : lines) {
            XSLFTextRun run = paragraph.addNewTextRun();
            run.setText(line);
            run.setFontSize(36.0);
            run.setBold(true);
            paragraph.addLineBreak();
        }
    }
}
