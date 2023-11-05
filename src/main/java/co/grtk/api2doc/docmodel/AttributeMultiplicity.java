package co.grtk.api2doc.docmodel;


import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum AttributeMultiplicity {
    ZERO_TO_ONE("0..1"),
    ZERO_TO_MORE("0..n"),
    ONE("1"),
    ONE_TO_MORE("1..n");

    @Getter
    private String display;

    public static AttributeMultiplicity fromParameter(Parameter parameter) {
        return fromSchema(parameter.getSchema(), parameter.getRequired());
    }

    public static AttributeMultiplicity fromSchema(Schema<?> schema, Boolean isRequired) {
        boolean isArray = schema instanceof ArraySchema;
        boolean required = Boolean.TRUE.equals(isRequired);
        if (isArray) {
            return required ? ONE_TO_MORE : ZERO_TO_MORE;
        } else {
            return required ? ONE : ZERO_TO_ONE;
        }

    }
}
