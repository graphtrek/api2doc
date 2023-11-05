package co.grtk.api2doc;


import co.grtk.api2doc.docmodel.ServiceDatasheet;
import co.grtk.api2doc.docmodel.SummaryDatasheet;
import co.grtk.api2doc.generator.ConfluenceGenerator;
import co.grtk.api2doc.generator.DocGenerator;
import co.grtk.api2doc.generator.WordGenerator;
import co.grtk.api2doc.loader.DocModelLoader;
import co.grtk.api2doc.loader.DocModelResult;
import co.grtk.api2doc.loader.OpenApiLoader;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@SpringBootApplication
@Slf4j
public class Api2DocApplication implements CommandLineRunner {

    @Value("${input}")
    private String input;

    @Value("${appDir}")
    private String appDir;

    @Value("${outputDir}")
    private String outputDir;

    @Value("${username}")
    private String username;

    @Value("${password}")
    private String password;

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(Api2DocApplication.class)
                .web(WebApplicationType.NONE)
                .run(args)
                .close();
    }


    @Override
    public void run(String... args) throws Exception {

        Path appPath = StringUtils.isNotBlank(appDir) ? Paths.get(appDir) : Paths.get("").toAbsolutePath();
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        Path outputPath;
        if (StringUtils.isNotBlank(outputDir)) {
            outputPath = Paths.get(outputDir).toAbsolutePath();
        } else if (appPath.equals(currentDir)) {
            outputPath = appPath.resolve("out");
        } else {
            outputPath = currentDir;
        }

        log.info("Starting application with input '{}' and output '{}'", input, outputPath);

        OpenApiLoader openApiLoader = new OpenApiLoader();
        SwaggerParseResult parseResult = openApiLoader.parse(input, username, password);
        List<String> warnings = new ArrayList<>(parseResult.getMessages());

        OpenAPI openAPI = parseResult.getOpenAPI();
        String title = openAPI.getInfo() != null ? StringUtils.defaultString(openAPI.getInfo().getTitle(), "NO_TITLE") : "NO_TITLE";
        log.info("OpenAPI specification '{}' parsed", title);
        outputPath = outputPath.resolve(title.replace(" ", ""));

        DocModelLoader docModelLoader = new DocModelLoader(openAPI);
        DocModelResult docModelResult = docModelLoader.load();
        List<ServiceDatasheet> serviceDatasheets = docModelResult.getServiceDatasheets();
        log.info("Document model build with {} service datasheets", serviceDatasheets.size());

        warnings.addAll(docModelResult.getWarnings());


        log.info("Generating word documents.");
        for (ServiceDatasheet serviceDatasheet : serviceDatasheets) {
            Path serviceOutputPath = outputPath.resolve(serviceDatasheet.getServiceName());
            DocGenerator wordGenerator = new WordGenerator(serviceOutputPath);
            serviceDatasheet.generate(wordGenerator);
        }


        log.info("Generating confluence documents.");
        for (ServiceDatasheet serviceDatasheet : serviceDatasheets) {
            DocGenerator confluenceGenerator = new ConfluenceGenerator(outputPath);
            serviceDatasheet.generate(confluenceGenerator);
        }

        DocGenerator summaryGenerator = new WordGenerator(outputPath);
        SummaryDatasheet summaryDatasheet = SummaryDatasheet.builder().serviceDatasheets(serviceDatasheets).build();
        summaryGenerator.generateSummarySheet(summaryDatasheet);

        if (!warnings.isEmpty()) {
            warnings.forEach(log::warn);
            log.warn("{} warnings found, the generated documents are not complete!", warnings.size());
        }
    }

}
