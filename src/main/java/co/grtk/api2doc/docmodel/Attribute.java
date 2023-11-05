package co.grtk.api2doc.docmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Model class representing an attribute, which can be a parameter, an object property, etc
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attribute {

    private String name;
    private AttributeStyle style;
    private AttributeType type;
    private String objectTypeName;
    private AttributeMultiplicity multiplicity;
    private String description;
    private ObjectDatasheet objectDatasheet;
    private List<String> enumValues;

    public String getTypeDisplay() {
        if (name.equalsIgnoreCase("empty response")) {
            return "";
        }
        String dispName = type != null ? type.getDisplay() : "undefined";
        if (type == AttributeType.OBJECT && objectTypeName != null) {
            dispName = objectTypeName;
        }
        if (multiplicity == AttributeMultiplicity.ZERO_TO_MORE || multiplicity == AttributeMultiplicity.ONE_TO_MORE) {
            dispName = "Array of " + dispName + "s";
        }
        return dispName;
    }

    public String getDescription() {
        if (description == null) {
            return null;
        }
        description = StringUtils.trimToEmpty(description);
        if (description.startsWith("\"") && description.endsWith("\"")) {
            description = description.substring(1, description.length() - 1);
        }
        if (description.startsWith("'") && description.endsWith("'")) {
            description = description.substring(1, description.length() - 1);
        }
        String possibleValues = "";
        if (enumValues != null && !enumValues.isEmpty() && !enumValues.stream().allMatch(description::contains)) {
            possibleValues = "\nPossible values: \n" + enumValues.stream().map(v -> "* " + v + "\n").collect(Collectors.joining());
        }
        return description + possibleValues;
    }

    public AttributeStyle getStyle() {
        if (style == null) {
            return AttributeStyle.NONE;
        }
        return style;
    }
}
