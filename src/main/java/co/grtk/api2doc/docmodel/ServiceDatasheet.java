package co.grtk.api2doc.docmodel;

import co.grtk.api2doc.generator.DocGenerator;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceDatasheet {

    private String serviceName;
    private String serviceVersion;
    private String entryPoint;
    private String httpMethod;
    private String requestDescription;
    @Builder.Default
    private List<Attribute> requestParameters = Collections.emptyList();
    private String responseDescription;
    private String responseType;
    @Builder.Default
    private List<Attribute> responseParameters = Collections.emptyList();
    @Builder.Default
    private List<Attribute> headerParameters = Collections.emptyList();
    @Builder.Default
    private List<ErrorCode> errorCodes = Collections.emptyList();
    private String shortDescription;


    public void generate(DocGenerator docGenerator) {
        docGenerator.prepare();
        Stream.of(requestParameters, responseParameters)
                .flatMap(List::stream)
                .filter(attr -> attr.getType() == AttributeType.OBJECT && attr.getObjectDatasheet() != null)
                .forEach(attr -> attr.getObjectDatasheet().generate(docGenerator));
        docGenerator.generateServiceSheet(this);
    }

}
