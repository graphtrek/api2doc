package co.grtk.api2doc.generator;

import co.grtk.api2doc.docmodel.Attribute;
import co.grtk.api2doc.docmodel.ObjectDatasheet;
import co.grtk.api2doc.docmodel.ServiceDatasheet;
import co.grtk.api2doc.docmodel.SummaryDatasheet;
import co.grtk.api2doc.exception.GenerationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@AllArgsConstructor
@Slf4j
public class WordGenerator implements DocGenerator {

    private static final String SERVICE_DATA_SHEET_NAME = "ServiceDatasheet-%s.docx";
    private static final String OBJECT_DATA_SHEET_NAME = "ObjectDatasheet-%s.docx";
    private static final String SUMMARY_DATA_SHEET_NAME = "SummaryDatasheet.docx";

    private final Path outputDir;

    @Override
    public void prepare() {
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new GenerationException(e.getMessage(), e);
        }
    }

    @Override
    public void generateServiceSheet(ServiceDatasheet serviceDatasheet) {
        InputStream template = this.getClass().getResourceAsStream("/template/ServiceDatasheet-template.docx");
        try (XWPFDocument doc = new XWPFDocument(OPCPackage.open(template))) {
            Path filePath = outputDir.resolve(String.format(SERVICE_DATA_SHEET_NAME, serviceDatasheet.getServiceName()));

            XWPFTable table = doc.getTables().get(0);
            writeTextToCell(table, 0, 1, serviceDatasheet.getServiceName());
            writeTextToCell(table, 1, 1, serviceDatasheet.getServiceVersion());
            writeTextToCell(table, 2, 1, serviceDatasheet.getEntryPoint());
            writeTextToCell(table, 3, 1, serviceDatasheet.getHttpMethod());
            writeTextToCell(table, 4, 1, serviceDatasheet.getRequestDescription());

            // REQUEST
            List<Attribute> attributes = serviceDatasheet.getRequestParameters();
            writeAttributes(table, attributes);

            // RESPONSE
            table = doc.getTables().get(1);
            writeTextToCell(table, 0, 1, serviceDatasheet.getResponseType());
            writeTextToCell(table, 1, 1, serviceDatasheet.getResponseDescription());
            attributes = serviceDatasheet.getResponseParameters();
            writeAttributes(table, attributes);

            // HEADER
            table = doc.getTables().get(2);
            attributes = serviceDatasheet.getHeaderParameters();
            writeAttributes(table, attributes, false);

            //ERROR CODES
            table = doc.getTables().get(3);
            writeTextToCell(table, 0, 1, "");
            writeTextToCell(table, 1, 1, serviceDatasheet.getShortDescription());

            if (!serviceDatasheet.getErrorCodes().isEmpty()) {
                XWPFRun run = table.getRow(0).getCell(1).getParagraphs().get(0).getRuns().get(0);
                serviceDatasheet.getErrorCodes().forEach(errorCode -> {
                    run.setText(errorCode.getCode() + " – " + errorCode.getDescription());
                    run.addBreak();
                });
            }

            Files.deleteIfExists(filePath);
            doc.write(new FileOutputStream(filePath.toFile()));
        } catch (Exception e) {
            throw new GenerationException(e.getMessage(), e);
        }
    }

    @Override
    public void generateSummarySheet(SummaryDatasheet summaryDatasheet) {
        InputStream template = this.getClass().getResourceAsStream("/template/Summary-template.docx");
        Path filePath = outputDir.resolve(SUMMARY_DATA_SHEET_NAME);
        try (XWPFDocument doc = new XWPFDocument(OPCPackage.open(template))) {
            XWPFTable table = doc.getTables().get(0);

            XWPFTableRow templateRow0 = table.getRow(0);
            CTRow ctRow0 = CTRow.Factory.parse(templateRow0.getCtRow().newInputStream());

            XWPFTableRow templateRow1 = table.getRow(1);
            CTRow ctRow1 = CTRow.Factory.parse(templateRow1.getCtRow().newInputStream());

            XWPFTableRow templateRow2 = table.getRow(2);
            CTRow ctRow2 = CTRow.Factory.parse(templateRow2.getCtRow().newInputStream());

            XWPFTableRow templateRow3 = table.getRow(3);
            CTRow ctRow3 = CTRow.Factory.parse(templateRow3.getCtRow().newInputStream());

            XWPFTableRow templateRow4 = table.getRow(4);
            CTRow ctRow4 = CTRow.Factory.parse(templateRow4.getCtRow().newInputStream());

            summaryDatasheet.getMap().forEach((tag,services) -> {
                log.info("SummarySheet tag:{} services:{}",tag, services.size());
                    XWPFTableRow row0 = new XWPFTableRow(ctRow0, table);
                    writeTextToCell(row0, 0, StringUtils.trimToEmpty(tag));
                    table.addRow(row0);

                    for (ServiceDatasheet serviceDatasheet :services) {
                        log.info("{} {} {}", serviceDatasheet.getHttpMethod(), serviceDatasheet.getEntryPoint(), serviceDatasheet.getServiceName());
                        XWPFTableRow row1 = new XWPFTableRow(ctRow1, table);
                        table.addRow(row1);

                        XWPFTableRow row2 = new XWPFTableRow(ctRow2, table);
                        writeTextToCell(row2, 0, StringUtils.trimToEmpty(serviceDatasheet.getServiceName()));
                        writeTextToCell(row2, 1, StringUtils.trimToEmpty(serviceDatasheet.getHttpMethod()));
                        writeTextToCell(row2, 2, StringUtils.trimToEmpty(serviceDatasheet.getEntryPoint()));
                        table.addRow(row2);

                        XWPFTableRow row3 = new XWPFTableRow(ctRow3, table);
                        writeTextToCell(row3, 0, StringUtils.trimToEmpty(serviceDatasheet.getShortDescription()));
                        writeTextToCell(row3, 1, StringUtils.trimToEmpty(serviceDatasheet.getRequestDescription()));
                        table.addRow(row3);

                    }
                XWPFTableRow row4 = new XWPFTableRow(ctRow4, table);
                table.addRow(row4);

            });

            Files.deleteIfExists(filePath);
            doc.write(new FileOutputStream(filePath.toFile()));
        } catch (Exception e) {
            throw new GenerationException(e.getMessage(), e);
        }

    }

    private void writeAttributes(XWPFTable table, List<Attribute> attributes) throws XmlException, IOException {
        writeAttributes(table, attributes, true);
    }

    private void writeAttributes(XWPFTable table, List<Attribute> attributes, boolean hasStyle) throws XmlException, IOException {

        int templateRowNum = table.getNumberOfRows() - 1;
        XWPFTableRow templateRow = table.getRow(templateRowNum);

        for (Attribute attribute : attributes) {
            CTRow ctrow = CTRow.Factory.parse(templateRow.getCtRow().newInputStream());
            XWPFTableRow row = new XWPFTableRow(ctrow, table);

            int cellIdx = 0;
            writeTextToCell(row, cellIdx++, StringUtils.trimToEmpty(attribute.getName()));
            if (hasStyle) {
                writeTextToCell(row, cellIdx++, attribute.getStyle() != null ? attribute.getStyle().getDisplay() : "");
            }
            if (attribute.getObjectDatasheet() != null) {
                writeTextToCell(row, cellIdx, "");
                XWPFHyperlinkRun run = createHyperlinkRun(row.getCell(cellIdx++).getParagraphs().get(0), attribute.getObjectDatasheet().getFileName());
                run.setText(attribute.getTypeDisplay());
                run.setUnderline(UnderlinePatterns.SINGLE);
                run.setColor("0563c1");
            } else {
                writeTextToCell(row, cellIdx++, attribute.getTypeDisplay());
            }
            writeTextToCell(row, cellIdx++, attribute.getMultiplicity() != null ? attribute.getMultiplicity().getDisplay() : "");
            writeTextToCell(row, cellIdx, attribute.getDescription());
            table.addRow(row);

        }
        table.removeRow(templateRowNum);
    }

    @Override
    public void generateObjectSheet(ObjectDatasheet objectDatasheet) {
        InputStream template = this.getClass().getResourceAsStream("/template/ObjectDatasheet-template.docx");
        try (XWPFDocument doc = new XWPFDocument(OPCPackage.open(template))) {
            Path filePath = outputDir.resolve(String.format(OBJECT_DATA_SHEET_NAME, objectDatasheet.getObjectName()));
            if (!objectDatasheet.isGenerated()) {
                objectDatasheet.setOutputPath(filePath);

                XWPFTable table = doc.getTables().get(0);
                writeTextToCell(table, 0, 1, objectDatasheet.getObjectName());
                writeTextToCell(table, 1, 1, objectDatasheet.getObjectVersion());
                writeTextToCell(table, 2, 1, objectDatasheet.getShortDescription());

                List<Attribute> attributes = objectDatasheet.getAttributes();
                writeAttributes(table, attributes);

                Files.deleteIfExists(filePath);
                doc.write(new FileOutputStream(filePath.toFile()));

                objectDatasheet.setGenerated(true);


            } else {
                if (!filePath.toFile().exists()) {
                    Files.copy(objectDatasheet.getOutputPath(), filePath);
                }
            }
        } catch (Exception e) {
            throw new GenerationException(e.getMessage(), e);
        }
    }


    private void writeTextToCell(XWPFTableRow row, int cell, String text) {
        XWPFRun run = row.getCell(cell).getParagraphs().get(0).getRuns().get(0);
        setText(run, text);
    }

    private void writeTextToCell(XWPFTable table, int row, int cell, String text) {
        XWPFRun run = table.getRow(row).getCell(cell).getParagraphs().get(0).getRuns().get(0);
        setText(run, text);
    }

    private void setText(XWPFRun run, String text) {
        if (text != null && text.contains("\n")) {
            String[] lines = text.split("\n");
            run.setText(lines[0], 0);
            for (int i = 1; i < lines.length; i++) {
                run.addBreak();
                run.setText(lines[i]);
            }
        } else {
            run.setText(text, 0);
        }
    }

    private static XWPFHyperlinkRun createHyperlinkRun(XWPFParagraph paragraph, String uri) {
        String rId = paragraph.getDocument().getPackagePart().addExternalRelationship(
                uri,
                XWPFRelation.HYPERLINK.getRelation()
        ).getId();

        CTHyperlink cthyperLink = paragraph.getCTP().addNewHyperlink();
        cthyperLink.setId(rId);
        cthyperLink.addNewR();

        return new XWPFHyperlinkRun(
                cthyperLink,
                cthyperLink.getRArray(0),
                paragraph
        );
    }
}
