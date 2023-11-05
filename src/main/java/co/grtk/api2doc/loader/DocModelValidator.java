package co.grtk.api2doc.loader;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class DocModelValidator {

    public void validate(List<String> errors, OpenAPI openAPI) {
        if (openAPI.getInfo() == null) {
            errors.add("API info is null.");
        } else if (StringUtils.isBlank(openAPI.getInfo().getTitle())) {
            errors.add("API title is empty");
        }
    }

    public void validate(List<String> errors, Operation operation) {
        if (StringUtils.isBlank(operation.getDescription())) {
            errors.add(String.format("Description is missing for operation %s", operation.getOperationId()));
        }
        if (StringUtils.isBlank(operation.getSummary())) {
            errors.add(String.format("Summary is missing for operation %s", operation.getOperationId()));
        }
    }

    public void validate(List<String> errors, Parameter parameter) {
        if (StringUtils.isBlank(parameter.getDescription())) {
            errors.add(String.format("Description is missing for parameter %s", parameter.getName()));
        }
    }
}
