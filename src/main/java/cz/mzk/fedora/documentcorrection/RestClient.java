package cz.mzk.fedora.documentcorrection;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.*;
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

public class RestClient {

    private final String fedoraHost;
    private final HttpEntity<String> httpEntity;
    private final RestTemplate restTemplate;
    private final DocumentBuilder xmlParser;
    private final Transformer xmlTransformer;
    private final XPathExpression xmlPathExp;

    public RestClient(String fh, String fu, String fp)
            throws ParserConfigurationException, XPathExpressionException, TransformerException {
        fedoraHost = fh;
        restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = createHttpHeaders(fu, fp);
        httpEntity = new HttpEntity<>(httpHeaders);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
        factory.setNamespaceAware(true);
        xmlParser = factory.newDocumentBuilder();

        XPathFactory xFactory = XPathFactory.newInstance();
        XPath xmlPath = xFactory.newXPath();
        xmlPathExp = xmlPath.compile("//*[local-name() = 'isMemberOfCollection']");

        TransformerFactory tFactory = TransformerFactory.newInstance();
        xmlTransformer = tFactory.newTransformer();
    }

    public Document removeAllVC(Document doc, String vc) throws XPathExpressionException {
        if (doc == null) { return null; }

        String uuidVC = getFullFormatVC(vc);
        NodeList nodes = (NodeList) xmlPathExp.evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getAttributes().item(0).getTextContent().equals(uuidVC) ||
                node.getAttributes().item(0).getTextContent().isEmpty()) {
                Node parent = node.getParentNode();
                if (parent != null) {
                    parent.removeChild(node);
                }
            }
        }
        return doc;
    }

    public void printAllVC(Document doc) throws XPathExpressionException {
        if (doc == null) { return; }

        NodeList nodes = (NodeList) xmlPathExp.evaluate(doc, XPathConstants.NODESET);
        System.out.println("Nodes length: " + nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            System.out.println("Node attribute: " + node.getAttributes().item(0).getTextContent());
        }
    }

    public Document getFoxmlByUuid(String uuid) {
        return getFedoraResource(fedoraHost + "/objects/" + uuid + "/objectXML");
    }

    // get fxml from fedora and return DOM document
    private Document getFedoraResource(String url) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            String str = Objects.requireNonNull(response.getBody());
            return xmlParser.parse(new InputSource(new StringReader(str)));
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.getMessage();
        }
        return null;
    }

    private HttpHeaders createHttpHeaders(String fu, String fp) {
        String credentials = fu + ":" + fp;
        String encodeCredentials = new String(Base64.encodeBase64(credentials.getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + encodeCredentials);
        return headers;
    }

    public String docToStr(Document doc) throws TransformerException {
        StreamResult streamResult = new StreamResult(new StringWriter());
        DOMSource domSource = new DOMSource(doc);
        xmlTransformer.transform(domSource, streamResult);
        return streamResult.getWriter().toString();
    }

    private String getFullFormatVC(String vc) {
        return "info:fedora/" + vc;
    }
}
