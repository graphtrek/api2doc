package co.grtk.api2doc.loader;

import co.grtk.api2doc.docmodel.ServiceDatasheet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DocModelResult {

    private List<ServiceDatasheet> serviceDatasheets;
    private List<String> warnings;
}
