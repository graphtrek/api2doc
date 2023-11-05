package co.grtk.api2doc.docmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorCode {

    private String code;
    private String description;

    public String getDescription() {
        if (description == null) {
            return null;
        }
        description = StringUtils.trimToEmpty(description);
        if (description.startsWith("\"") && description.endsWith("\"")) {
            return description.substring(1, description.length() - 1);
        }
        if (description.startsWith("'") && description.endsWith("'")) {
            return description.substring(1, description.length() - 1);
        }

        return description;
    }
}
