package co.grtk.api2doc.generator;

import co.grtk.api2doc.docmodel.ObjectDatasheet;
import co.grtk.api2doc.docmodel.ServiceDatasheet;
import co.grtk.api2doc.docmodel.SummaryDatasheet;
import co.grtk.api2doc.exception.GenerationException;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public class ConfluenceGenerator implements DocGenerator {

    private TemplateEngine templateEngine;
    private Path outputDir;

    private Map<String, ObjectDatasheet> objectDatasheets = new TreeMap<>();

    public ConfluenceGenerator(Path outputDir) {
        this.outputDir = outputDir.resolve("_confluence");
        templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/template/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setTemplateMode(TemplateMode.HTML);
        templateEngine.setTemplateResolver(resolver);
    }

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
        try {
            Context ct = new Context();
            ct.setVariable("service", serviceDatasheet);
            ct.setVariable("objectDataSheets", objectDatasheets.values());
            ct.setVariable("newLineChar", StringUtils.LF);
            Writer writer = new FileWriter(outputDir.resolve(serviceDatasheet.getServiceName() + "_code.txt").toString());
            templateEngine.process("confluence.html", ct, writer);
        } catch (Exception e) {
            throw new GenerationException(e.getMessage(), e);
        }
    }

    @Override
    public void generateSummarySheet(SummaryDatasheet serviceDatasheet) {

    }

    @Override
    public void generateObjectSheet(ObjectDatasheet objectDatasheet) {
        objectDatasheets.put(objectDatasheet.getObjectName(), objectDatasheet);
    }
}
