package cz.mzk.fedora.documentcorrection;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Objects;
import java.util.Optional;

public class FedoraRestClient {

    private final String fedoraHost;
    private final HttpEntity<String> httpEntity;
    private final RestTemplate restTemplate;
    private final DocumentBuilder xmlParser;
    private final Transformer xmlTransformer;
    private final XPathExpression xmlPathExp;

    public FedoraRestClient(String fh, String fu, String fp)
            throws ParserConfigurationException, XPathExpressionException, TransformerException {
        fedoraHost = fh;
        restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = createHttpHeaders(fu, fp);
        httpEntity = new HttpEntity<>(httpHeaders);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
        //factory.setNamespaceAware(true);
        xmlParser = factory.newDocumentBuilder();

        XPathFactory xFactory = XPathFactory.newInstance();
        XPath xmlPath = xFactory.newXPath();
        xmlPathExp = xmlPath.compile("//*[local-name() = 'isMemberOfCollection']/@resource");

        TransformerFactory tFactory = TransformerFactory.newInstance();
        xmlTransformer = tFactory.newTransformer();
    }

    public Optional<Document> removeVc(Optional<Document> doc, String vc)
            throws XPathExpressionException {
        if ((doc.isPresent()) && (vc != null)) {
            String uuidVc = getFullFormatVC(vc);
            NodeList nodes = (NodeList) xmlPathExp.evaluate(doc.get(), XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getTextContent().equals(uuidVc)) {
                    Attr attr = (Attr) node;
                    Node nodeOwner = attr.getOwnerElement();
                    if (nodeOwner.getParentNode() != null) {
                        nodeOwner.getParentNode().removeChild(nodeOwner);
                    }
                }
            }
        }
        return doc;
    }

    public void printAllVC(Optional<Document> doc) throws XPathExpressionException {
        if (doc.isPresent()) {
            NodeList nodes = (NodeList) xmlPathExp.evaluate(doc, XPathConstants.NODESET);
            System.out.println("Nodes length: " + nodes.getLength());

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                System.out.println(node.getTextContent());
            }
        }
    }

    public Optional<Document> getFoxmlByUuid(String uuid) {
        return getFedoraResource(fedoraHost + "/objects/" + uuid + "/objectXML");
    }

    // get fxml from fedora and return DOM document
    private Optional<Document> getFedoraResource(String url) {
        Document responseDoc = null;

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            String str = Objects.requireNonNull(response.getBody());
            responseDoc =  xmlParser.parse(new InputSource(new StringReader(str)));
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        } catch (HttpClientErrorException e) {
            e.getMessage();
        }
        return Optional.ofNullable(responseDoc);
    }

    private HttpHeaders createHttpHeaders(String fu, String fp) {
        String credentials = fu + ":" + fp;
        String encodeCredentials = new String(Base64.encodeBase64(credentials.getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + encodeCredentials);
        return headers;
    }

    public Optional<String> docToStr(Optional<Document> doc) throws TransformerException {
        String str = null;

        if (doc.isPresent()) {
            StreamResult streamResult = new StreamResult(new StringWriter());
            DOMSource domSource = new DOMSource(doc.get());
            xmlTransformer.transform(domSource, streamResult);
            str = streamResult.getWriter().toString();
        }
        return Optional.ofNullable(str);
    }

    private String getFullFormatVC(String vc) {
        return "info:fedora/" + vc;
    }
}
