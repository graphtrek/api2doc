package co.grtk.api2doc.generator;

import co.grtk.api2doc.docmodel.ObjectDatasheet;
import co.grtk.api2doc.docmodel.ServiceDatasheet;
import co.grtk.api2doc.docmodel.SummaryDatasheet;

/**
 * Document generator interface
 */
public interface DocGenerator {

    void prepare();

    void generateServiceSheet(ServiceDatasheet serviceDatasheet);

    void generateSummarySheet(SummaryDatasheet summaryDatasheet);

    void generateObjectSheet(ObjectDatasheet objectDatasheet);
}
