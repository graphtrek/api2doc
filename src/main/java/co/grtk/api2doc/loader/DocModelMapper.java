package co.grtk.api2doc.loader;

import co.grtk.api2doc.docmodel.AttributeStyle;
import co.grtk.api2doc.docmodel.AttributeType;
import co.grtk.api2doc.docmodel.Attribute;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

import static co.grtk.api2doc.docmodel.AttributeMultiplicity.fromParameter;

public class DocModelMapper {

    public Attribute mapParameter(Parameter parameter) {
        List<String> enumValues = Collections.emptyList();
        if (parameter.getSchema() instanceof StringSchema) {
            StringSchema stringSchema = (StringSchema) parameter.getSchema();
            enumValues = stringSchema.getEnum();
        }
        return Attribute.builder()
                .name(parameter.getName())
                .style(AttributeStyle.valueOf(parameter.getIn().toUpperCase()))
                .type(AttributeType.fromSchema(parameter.getSchema()))
                .multiplicity(fromParameter(parameter))
                .description(parameter.getDescription())
                .enumValues(enumValues)
                .build();
    }

    public String deductOperationId(String httpMethod, String path) {
        StringBuilder sb = new StringBuilder(httpMethod.toLowerCase());
        String name = path;
        String pathParam = null;
        if (name.indexOf('/') >= 0) {
            name = name.substring(name.lastIndexOf('/') + 1);
            if (name.startsWith("{") && name.endsWith("}")) {
                pathParam = name.substring(1, name.length() - 1);
                String urlPart = path.substring(0, path.lastIndexOf('/'));
                name = urlPart.indexOf('/') >= 0 ? urlPart.substring(urlPart.lastIndexOf('/') + 1) : "";
            }
        }
        sb.append(StringUtils.capitalize(name));
        if (pathParam != null) {
            sb.append("By")
                    .append(StringUtils.capitalize(pathParam));
        }

        return sb.toString();
    }


}
