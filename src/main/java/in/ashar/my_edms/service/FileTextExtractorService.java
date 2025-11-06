package in.ashar.my_edms.service;

import in.ashar.my_edms.entity.Metadata;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
public class FileTextExtractorService {

    public Metadata extractText(MultipartFile file, String docId) throws IOException {

        String sb = "Unable to extract";

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("Invalid file: no name provided.");
        }
        String lowerName = fileName.toLowerCase();

        if (lowerName.endsWith(".pdf")) {
            sb = extractFromPdf(file);
        } else if (lowerName.endsWith(".docx") || lowerName.endsWith(".doc")) {
            sb = extractFromWord(file);
        } else if (lowerName.endsWith(".xls") || lowerName.endsWith(".xlsx")) {
            sb = extractFromExcel(file);
        } else if (lowerName.endsWith(".txt")) {
            sb = extractFromTxt(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + fileName);
        }

        Metadata metadata = new Metadata();

        metadata.setDocId(docId);
        metadata.setTitle(fileName);
        metadata.setDescription(sb);
        metadata.setTags(lowerName);

        return metadata;

    }

    private String extractFromTxt(MultipartFile file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }


    private String extractFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }


    private String extractFromWord(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            String lowerName = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
            if (lowerName.endsWith(".docx")) {
                try (XWPFDocument docx = new XWPFDocument(is);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(docx)) {
                    String text = extractor.getText();
                    return text.replaceAll("\\r\\n+", "\n").trim();

                }
            } else {
                try (HWPFDocument doc = new HWPFDocument(is);
                     WordExtractor extractor = new WordExtractor(doc)) {
                    String text = extractor.getText();
                    return text.replaceAll("\\r\\n+", "\n").trim();

                }
            }
        }
    }

    private String extractFromExcel(MultipartFile file) throws IOException {
        StringBuilder sb = new StringBuilder();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            DataFormatter formatter = new DataFormatter(); // keeps cell formatting
            for (Sheet sheet : workbook) {
                sb.append("Sheet: ").append(sheet.getSheetName()).append("\n");

                for (Row row : sheet) {
                    for (Cell cell : row) {
                        String cellValue = formatter.formatCellValue(cell);
                        sb.append(cellValue).append("\t");
                    }
                    sb.append("\n");
                }

                sb.append("\n");
            }
        }

        return sb.toString();
    }


}
