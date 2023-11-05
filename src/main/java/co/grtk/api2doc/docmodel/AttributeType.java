package co.grtk.api2doc.docmodel;

import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ByteArraySchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.EmailSchema;
import io.swagger.v3.oas.models.media.FileSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.PasswordSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.UUIDSchema;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public enum AttributeType {
    STRING("String", StringSchema.class, EmailSchema.class, PasswordSchema.class, UUIDSchema.class),
    BINARY("File", BinarySchema.class, FileSchema.class, ByteArraySchema.class),
    ENUM("ENUM"),
    DATE("Date", DateSchema.class),
    DATE_TIME("DateTime", DateTimeSchema.class),
    INTEGER("Integer", IntegerSchema.class),
    NUMBER("Number", NumberSchema.class),
    BOOLEAN("Boolean", BooleanSchema.class),
    OBJECT("Object", ObjectSchema.class, MapSchema.class),
    UNDEFINED("undefined");


    @Getter
    private String display;
    private List<Class<?>> oasSchemas = new ArrayList<>();

    private AttributeType(String display, Class<?>... oasSchemas) {
        this.display = display;
        if (oasSchemas != null) {
            this.oasSchemas.addAll(Arrays.asList(oasSchemas));
        }
    }

    public static AttributeType fromSchema(Schema<?> oasSchema) {
        if (oasSchema instanceof StringSchema && oasSchema.getEnum() != null && !oasSchema.getEnum().isEmpty()) {
            return AttributeType.ENUM;
        }

        return Stream.of(values())
                .filter(e -> e.oasSchemas.contains(oasSchema.getClass()))
                .findFirst()
                .orElse(null);
    }
}
