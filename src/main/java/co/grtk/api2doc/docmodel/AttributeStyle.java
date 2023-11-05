package co.grtk.api2doc.docmodel;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;


public enum AttributeStyle {
    QUERY("Query"),
    HEADER("Header"),
    PATH("Path"),
    COOKIE("Cookie"),
    JSON("JSON", "application/json"),
    FORM_DATA("Form Data", "multipart/form-data", "application/x-www-form-urlencoded"),
    BINARY("Binary", "application/octet-stream"),
    REQUEST_BODY("Request Body"),
    RESPONSE_BODY("Response Body"),
    NONE("");

    @Getter
    private String display;

    @Getter
    private List<String> contentTypes = new ArrayList<>();

    private AttributeStyle(String display, String... contentTypes) {
        this.display = display;
        if (contentTypes != null) {
            this.contentTypes.addAll(Arrays.asList(contentTypes));
        }
    }

    public static AttributeStyle fromContentType(String contentType) {
        return Stream.of(values())
                .filter(e -> e.contentTypes.contains(contentType.toLowerCase()))
                .findFirst().orElse(null);
    }
}
