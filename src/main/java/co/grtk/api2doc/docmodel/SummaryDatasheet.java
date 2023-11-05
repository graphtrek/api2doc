package co.grtk.api2doc.docmodel;

import co.grtk.api2doc.generator.DocGenerator;
import lombok.*;

import java.util.*;

@Getter
@Setter
public class SummaryDatasheet {

    private List<ServiceDatasheet> serviceDatasheets = Collections.emptyList();
    private Map<String, List<ServiceDatasheet>> map;

    public SummaryDatasheet(List<ServiceDatasheet> serviceDatasheets) {
        this.serviceDatasheets = serviceDatasheets;
        map = new HashMap<>();
        serviceDatasheets.forEach(serviceDatasheet -> serviceDatasheet.getTags().forEach(tag -> {
            if(!map.containsKey(tag)) {
                List<ServiceDatasheet> list = new ArrayList<>();
                list.add(serviceDatasheet);
                map.put(tag, list);
            } else {
                List<ServiceDatasheet> list = map.get(tag);
                list.add(serviceDatasheet);
            }
        }));
    }

    public void generate(DocGenerator docGenerator) {
        serviceDatasheets.sort(Comparator.comparing(ServiceDatasheet::getEntryPoint));
        docGenerator.generateSummarySheet(this);
    }
}
