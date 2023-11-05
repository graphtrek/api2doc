package co.grtk.api2doc.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class OpenApiLoadException extends Exception {

    private final List<String> messages;
}
