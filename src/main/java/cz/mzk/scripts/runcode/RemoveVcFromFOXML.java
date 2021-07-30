package cz.mzk.scripts.runcode;

import cz.mzk.scripts.clientapi.FedoraRestClient;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.util.Optional;

public class RemoveVcFromFOXML {

    public static void main(String[] args)
            throws ParserConfigurationException, XPathExpressionException, TransformerException {
        String fh = System.getenv("FEDORA_HOST");
        String fu = System.getenv("FEDORA_USER");
        String fp = System.getenv("FEDORA_PASSWD");

        String vc = "";
        String[] uuids = {""};


        FedoraRestClient fedoraRestClient = new FedoraRestClient(fh, fu, fp);

        for (String uuid : uuids) {
            Optional<Document> doc = fedoraRestClient.getRelsExt(uuid);

            if (doc.isPresent()) {
                doc = fedoraRestClient.removeVc(doc.get(), vc);
                if (doc.isPresent()) {
                    fedoraRestClient.setRelsExt(uuid, doc.get());
                }
            }
        }

    }

}
