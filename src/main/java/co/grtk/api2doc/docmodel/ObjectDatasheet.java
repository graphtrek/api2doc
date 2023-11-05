package co.grtk.api2doc.docmodel;

import co.grtk.api2doc.generator.DocGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Model representing the object datasheet document
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class ObjectDatasheet {

    private String objectName;
    private String objectVersion;
    private String shortDescription;
    @Builder.Default
    private List<Attribute> attributes = Collections.emptyList();

    private Path outputPath;
    private boolean generated;

    public void generate(DocGenerator docGenerator) {
        if (attributes == null) {
            log.warn("Empty object datasheet found!");
            return;
        }
        attributes.stream()
                .filter(attr -> attr.getType() == AttributeType.OBJECT && attr.getObjectDatasheet() != null)
                .forEach(attr -> attr.getObjectDatasheet().generate(docGenerator));
        docGenerator.generateObjectSheet(this);
    }

    public String getFileName() {
        return outputPath != null ? outputPath.getFileName().toString() : null;
    }
}
