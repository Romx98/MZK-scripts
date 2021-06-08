package cz.mzk.fedora.documentcorrection;

import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

public class RunRestClient {

    public static void main(String[] args)
            throws ParserConfigurationException, XPathExpressionException {
        String fh = System.getenv("FH");
        String fu = System.getenv("FU");
        String fp = System.getenv("FP");
        String uuid = "";
        String vc = "";

        RestClient restClient = new RestClient(fh, fu, fp);

        Document doc = restClient.getFoxmlByUuid(uuid);
        restClient.printAllVC(doc);
    }

}
