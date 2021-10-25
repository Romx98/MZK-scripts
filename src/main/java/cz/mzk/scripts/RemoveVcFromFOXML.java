package cz.mzk.scripts;

import cz.mzk.rest.FedoraRestClient;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Optional;

public class RemoveVcFromFOXML {

    public static void main(String[] args)
            throws ParserConfigurationException, XPathExpressionException, TransformerException, IOException {
        String fh = System.getenv("FEDORA_HOST");
        String fu = System.getenv("FEDORA_USER");
        String fp = System.getenv("FEDORA_PASSWD");
        String uuid = "";
        String vc = "";

        FedoraRestClient fedoraRestClient = new FedoraRestClient(fh, fu, fp);
        Optional<Document> doc = fedoraRestClient.getRelsExt(uuid);

        if (doc.isPresent()) {
            doc = fedoraRestClient.removeVc(doc.get(), vc);
            if (doc.isPresent()) {
                fedoraRestClient.setRelsExt(uuid, doc.get());
            }
        }
    }

}
