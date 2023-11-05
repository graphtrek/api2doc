package co.grtk.api2doc.loader;

import co.grtk.api2doc.exception.OpenApiLoadException;
import io.swagger.v3.parser.OpenAPIResolver;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;

@Slf4j
public class OpenApiLoader {

    public static final String API_OPENAPI_JSON = "api/openapi.json";

    public SwaggerParseResult parse(String input, String userName, String password) throws OpenApiLoadException {
        OpenAPIV3Parser parser = new OpenAPIV3Parser();

        SwaggerParseResult result;

        if (input.toLowerCase().startsWith("http")) {
            String basicAuth = new String(Base64.getEncoder().encode((userName + ":" + password).getBytes()));
            AuthorizationValue auth = new AuthorizationValue("Authorization", "Basic " + basicAuth, "header");
            String location = input;
            if (!input.toLowerCase().endsWith(".json") && !input.toLowerCase().endsWith(".yaml")) {
                location = location.endsWith("/") ? location : location + "/";
                location += API_OPENAPI_JSON;
            }
            result = parser.readWithInfo(location, Collections.singletonList(auth));
        } else {
            File inputFile = Paths.get(input).toFile();
            if (!inputFile.exists() || !inputFile.isFile() || !inputFile.canRead()) {
                throw new OpenApiLoadException(Collections.singletonList("Input file does not exists or it is no readable"));
            }

            result = parser.readWithInfo(input, Collections.emptyList());
        }
        if (result.getMessages() != null && !result.getMessages().isEmpty() && result.getOpenAPI() == null) {
            throw new OpenApiLoadException(result.getMessages());
        }
        if (result.getMessages() == null) {
            result.setMessages(Collections.emptyList());
        }

        OpenAPIResolver openAPIResolver = new OpenAPIResolver(result.getOpenAPI());
        result.setOpenAPI(openAPIResolver.resolve());
        return result;
    }
}
