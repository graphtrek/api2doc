package co.grtk.api2doc.docmodel;

import co.grtk.api2doc.generator.DocGenerator;
import lombok.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryDatasheet {

    @Builder.Default
    private List<ServiceDatasheet> serviceDatasheets = Collections.emptyList();

    public void generate(DocGenerator docGenerator) {
        serviceDatasheets.sort(Comparator.comparing(ServiceDatasheet::getEntryPoint));
        docGenerator.generateSummarySheet(this);
    }

}
