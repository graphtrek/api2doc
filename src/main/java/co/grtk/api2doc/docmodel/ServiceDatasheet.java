package co.grtk.api2doc.docmodel;

import co.grtk.api2doc.generator.DocGenerator;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
    @Builder.Default
    private List<String> tags = Collections.emptyList();

    public void generate(DocGenerator docGenerator) {
        Stream.of(requestParameters, responseParameters)
                .flatMap(List::stream)
                .filter(attr -> attr.getType() == AttributeType.OBJECT && attr.getObjectDatasheet() != null)
                .forEach(attr -> attr.getObjectDatasheet().generate(docGenerator));
        docGenerator.generateServiceSheet(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceDatasheet that)) return false;
        return Objects.equals(serviceName, that.serviceName) &&
                Objects.equals(serviceVersion, that.serviceVersion) &&
                Objects.equals(entryPoint, that.entryPoint) &&
                Objects.equals(httpMethod, that.httpMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, serviceVersion, entryPoint, httpMethod);
    }
}
