package co.grtk.api2doc.loader;

import co.grtk.api2doc.exception.InvalidApiException;
import co.grtk.api2doc.docmodel.Attribute;
import co.grtk.api2doc.docmodel.AttributeMultiplicity;
import co.grtk.api2doc.docmodel.AttributeStyle;
import co.grtk.api2doc.docmodel.AttributeType;
import co.grtk.api2doc.docmodel.ErrorCode;
import co.grtk.api2doc.docmodel.ObjectDatasheet;
import co.grtk.api2doc.docmodel.ServiceDatasheet;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.util.RefUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class DocModelLoader {

    private final Map<String, ObjectDatasheet> objectsMap = new HashMap<>();
    private final DocModelMapper modelMapper = new DocModelMapper();
    private final DocModelValidator validator = new DocModelValidator();
    private final List<String> warnings = new ArrayList<>();

    private OpenAPI openAPI;

    public DocModelLoader(OpenAPI openAPI) {
        this.openAPI = openAPI;
    }

    public DocModelResult load() {

        List<ServiceDatasheet> serviceDatasheets = new ArrayList<>();
        validator.validate(warnings, openAPI);

        openAPI.getPaths().forEach((path, item) -> {

            Map<String, Attribute> itemLevelParams = getItemLevelParams(item.getParameters());
            item.readOperationsMap().forEach(((httpMethod, operation) -> {
                String operationId = operation.getOperationId();
                if (StringUtils.isBlank(operationId)) {
                    warnings.add(String.format("Operation %s %s has not operationId defined, will be composed from method and path.", httpMethod.name(), path));
                    operationId = modelMapper.deductOperationId(httpMethod.name(), path);
                    operation.setOperationId(operationId);
                }


                serviceDatasheets.add(load(httpMethod, path, operation, itemLevelParams));

            }));
        });


        return new DocModelResult(serviceDatasheets, warnings);
    }

    private Map<String, Attribute> getItemLevelParams(List<Parameter> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Attribute> attributeMap = new LinkedHashMap<>();
        parameters.forEach(parameter -> attributeMap.put(parameter.getName(), convert(parameter)));
        return attributeMap;
    }


    private ServiceDatasheet load(PathItem.HttpMethod httpMethod, String path, Operation operation, Map<String, Attribute> itemLevelParams) {
        validator.validate(warnings, operation);

        ServiceDatasheet serviceDatasheet = ServiceDatasheet.builder()
                .serviceName(operation.getOperationId())
                .serviceVersion(openAPI.getInfo() != null ? openAPI.getInfo().getVersion() : "v1.0")
                .entryPoint(path)
                .httpMethod(httpMethod.name())
                .requestDescription(operation.getDescription())
                .shortDescription(operation.getSummary())
                .build();

        //REQUEST
        Map<String, Attribute> requestParamMap = new LinkedHashMap<>(itemLevelParams);
        if (operation.getParameters() != null) {
            List<Attribute> headerParameters = new ArrayList<>();

            // HEADER PARAMETERS
            operation.getParameters().stream()
                    .filter(param -> param instanceof HeaderParameter)
                    .map(HeaderParameter.class::cast)
                    .forEach(param -> headerParameters.add(convert(param)));

            serviceDatasheet.setHeaderParameters(headerParameters);
            //OTHER PARAMETERS
            operation.getParameters().stream()
                    .filter(param -> !(param instanceof HeaderParameter))
                    .forEach(parameter -> requestParamMap.put(parameter.getName(), convert(parameter)));
        }
        //REQUEST BODY
        if (operation.getRequestBody() != null) {
            requestParamMap.put("RequestBody", convert(operation.getRequestBody()));
        }

        List<Attribute> requestParameters = new ArrayList<>(requestParamMap.values());
        requestParameters.sort(Comparator.comparing(Attribute::getStyle));
        serviceDatasheet.setRequestParameters(requestParameters);

        //RESPONSE
        List<Attribute> responseParameters = new ArrayList<>();
        ApiResponse apiResponse = operation.getResponses().getDefault();
        if (apiResponse == null) {
            apiResponse = operation.getResponses().get("200");
        }
        if (apiResponse != null) {
            Attribute responseAttribute = convert(apiResponse);
            serviceDatasheet.setResponseType(responseAttribute.getObjectTypeName());
            serviceDatasheet.setResponseDescription(responseAttribute.getDescription());
            if (responseAttribute.getObjectDatasheet() != null) {
                responseParameters.addAll(responseAttribute.getObjectDatasheet().getAttributes());
            } else {
                responseParameters.add(responseAttribute);
            }
        }
        serviceDatasheet.setResponseParameters(responseParameters);

        //ERROR CODES
        serviceDatasheet.setErrorCodes(new ArrayList<>());
        operation.getResponses().forEach((code, response) -> {
            if (!"200".equals(code)) {
                serviceDatasheet.getErrorCodes().add(new ErrorCode(code, response.getDescription()));
            }
        });

        return serviceDatasheet;
    }

    private <T> Optional<T> resolveRef(String refName, Map<String, T> object) {
        if (object != null && object.containsKey(refName)) {
            return Optional.of(object.get(refName));
        }
        return Optional.empty();
    }

    private Attribute convert(Parameter parameter) {
        if (parameter == null) {
            throw new InvalidApiException("Cannot convert null parameter");
        }
        if (parameter.get$ref() != null) {
            log.info("Resolving parameter reference {}", parameter.get$ref());
            String typeName = RefUtils.computeDefinitionName(parameter.get$ref());
            parameter = resolveRef(typeName, openAPI.getComponents().getParameters())
                    .orElseThrow(() -> new InvalidApiException("Cannot convert null parameter"));
        }
        validator.validate(warnings, parameter);
        return modelMapper.mapParameter(parameter);
    }

    private Attribute convert(RequestBody requestBody) {

        String typeName = "RequestBodyType";
        if (requestBody.get$ref() != null) {
            log.info("Resolving requestBody reference {}", requestBody.get$ref());
            typeName = RefUtils.computeDefinitionName(requestBody.get$ref());
            requestBody = resolveRef(typeName, openAPI.getComponents().getRequestBodies())
                    .orElseThrow(() -> new InvalidApiException("Cannot resolve request body reference"));
        }
        boolean required = Boolean.TRUE.equals(requestBody.getRequired());
        String description = requestBody.getDescription();

        if (requestBody.getContent().size() == 1) {
            String contentType = requestBody.getContent().keySet().iterator().next();
            MediaType mediaType = requestBody.getContent().get(contentType);
            Schema<?> schema = mediaType.getSchema();

            return convert(schema, required, "RequestBody", typeName, description, AttributeStyle.fromContentType(contentType));
        } else {
            throw new InvalidApiException("Multiple content-type for request body is not supported");
        }

    }

    private Attribute convert(ApiResponse apiResponse) {
        String typeName = "ResponseType";
        String description = apiResponse.getDescription();
        if (apiResponse.get$ref() != null && !apiResponse.get$ref().endsWith("/null")) {
            log.info("Resolving response reference {}", apiResponse.get$ref());
            typeName = RefUtils.computeDefinitionName(apiResponse.get$ref());
            apiResponse = resolveRef(typeName, openAPI.getComponents().getResponses())
                    .orElseThrow(() -> new InvalidApiException("Cannot resolve response reference"));
        }
        description = StringUtils.defaultString(apiResponse.getDescription(), description);
        if (apiResponse.getContent() == null || apiResponse.getContent().isEmpty()) {
            return Attribute.builder()
                    .name("Empty response")
                    .build();
        }
        if (apiResponse.getContent().size() == 1) {
            String contentType = apiResponse.getContent().keySet().iterator().next();
            MediaType mediaType = apiResponse.getContent().get(contentType);
            Schema<?> schema = mediaType.getSchema();
            AttributeStyle attributeStyle = AttributeStyle.fromContentType(contentType);
            if (attributeStyle == AttributeStyle.BINARY || schema == null) {
                return Attribute.builder()
                        .name("ResponseBody")
                        .type(schema == null ? AttributeType.UNDEFINED : AttributeType.BINARY)
                        .multiplicity(AttributeMultiplicity.ZERO_TO_ONE)
                        .style(AttributeStyle.RESPONSE_BODY)
                        .description(description)
                        .build();
            }
            return convert(schema, false, "ResponseBody", typeName, description, attributeStyle);
        } else {
            throw new InvalidApiException("Response with multiple content type not supported");
        }
    }

    private Attribute convert(Schema<?> schema, boolean required, String name, String typeNameParam, String descriptionParam, AttributeStyle attributeStyle) {
        if (schema == null || StringUtils.isBlank(name)) {
            throw new InvalidApiException("Cannot convert null schema or empty name");
        }
        String typeName = typeNameParam;
        String description = descriptionParam;
        AttributeMultiplicity multiplicity = AttributeMultiplicity.fromSchema(schema, required);

        if (schema instanceof ArraySchema) {
            schema = ((ArraySchema) schema).getItems();
        }
        if (schema.get$ref() != null && !schema.get$ref().endsWith("/null")) {
            log.info("Resolving schema reference {}", schema.get$ref());
            typeName = RefUtils.computeDefinitionName(schema.get$ref());
            schema = resolveRef(typeName, openAPI.getComponents().getSchemas())
                    .orElseThrow(() -> new InvalidApiException("Cannot resolve schema reference"));
        }
        AttributeType attributeType = AttributeType.fromSchema(schema);
        description = StringUtils.defaultString(schema.getDescription(), description);
        typeName = StringUtils.defaultString(typeName, name + "Type");
        typeName = StringUtils.defaultString(schema.getName(), typeName);

        ObjectDatasheet objectDatasheet = null;
        if (attributeType == AttributeType.OBJECT) {
            objectDatasheet = createObjectDatasheet(schema, typeName, description);
        }
        if (StringUtils.isBlank(description)) {
            warnings.add(String.format("Description is missing for for attribute %s", name));
        }
        return Attribute.builder()
                .name(name)
                .description(description)
                .style(attributeType == AttributeType.BINARY ? AttributeStyle.FORM_DATA : attributeStyle)
                .type(attributeType)
                .enumValues(attributeType == AttributeType.ENUM ? ((StringSchema) schema).getEnum() : Collections.emptyList())
                .multiplicity(multiplicity)
                .objectDatasheet(objectDatasheet)
                .objectTypeName(typeName)
                .build();
    }

    private ObjectDatasheet createObjectDatasheet(Schema<?> schema, String typeName, String description) {
        ObjectDatasheet objectDatasheet = null;
        if (objectsMap.containsKey(typeName)) {
            objectDatasheet = objectsMap.get(typeName);
        } else if (schema.getProperties() != null) {
            objectDatasheet = ObjectDatasheet.builder()
                    .objectName(typeName)
                    .objectVersion(openAPI.getInfo() != null ? openAPI.getInfo().getVersion() : "v1.0")
                    .shortDescription(description)
                    .attributes(Collections.emptyList())
                    .build();


            List<Attribute> attributeAttributes = new ArrayList<>();
            for (String propName : schema.getProperties().keySet()) {

                boolean propRequired = schema.getRequired() != null && schema.getRequired().contains(propName);
                Schema<?> propSchema = schema.getProperties().get(propName);
                Attribute propAttribute = convert(propSchema, propRequired, propName, propSchema.getName(), propSchema.getDescription(), AttributeStyle.JSON);
                attributeAttributes.add(propAttribute);
            }
            objectDatasheet.setAttributes(attributeAttributes);
            objectsMap.put(typeName, objectDatasheet);
        } else {
            log.warn("Object schema without properties.");
        }
        return objectDatasheet;
    }

}
