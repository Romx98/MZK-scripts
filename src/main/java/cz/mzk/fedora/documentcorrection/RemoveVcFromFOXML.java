package cz.mzk.fedora.documentcorrection;

import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Optional;

public class RemoveVcFromFOXML {

    public static void main(String[] args)
            throws ParserConfigurationException, XPathExpressionException, TransformerException, IOException {
        String fh = System.getenv("FH");
        String fu = System.getenv("FU");
        String fp = System.getenv("FP");
        String uuid = "";
        String vc = "";

        FedoraRestClient fedoraRestClient = new FedoraRestClient(fh, fu, fp);
        Optional<Document> doc = fedoraRestClient.getRelsExt(uuid);

        if (doc.isPresent()) {
            doc = fedoraRestClient.removeVc(doc.get(), vc);
            if (doc.isPresent()) {
                System.out.println(fedoraRestClient.docToStr(doc.get()));
                fedoraRestClient.setRelsExt(uuid, doc.get());
            }
        }
    }

}
