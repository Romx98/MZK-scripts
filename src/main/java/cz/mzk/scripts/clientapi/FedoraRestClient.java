package cz.mzk.scripts.clientapi;

import cz.mzk.scripts.configuration.ClientApiConfig;
import cz.mzk.scripts.model.DataStreams;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
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
import java.util.*;

public class FedoraRestClient {

    private final String fedoraHost;
    private final HttpEntity<String> httpEntity;
    private final HttpHeaders httpHeaders;
    private final RestTemplate restTemplate;
    private final DocumentBuilder xmlParser;
    private final Transformer xmlTransformer;
    private final XPathExpression xmlPathExp;

    public FedoraRestClient(String fh, String fu, String fp)
            throws ParserConfigurationException, XPathExpressionException, TransformerException {
        fedoraHost = fh;
        restTemplate = ClientApiConfig.getConfiguredRestTemplate();
        httpHeaders = ClientApiConfig.createHttpHeaders(fu, fp);
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

    public Optional<Document> removeVc(Document doc, String vc)
            throws XPathExpressionException {
        if ((doc != null) && (vc != null)) {
            String uuidVc = getFullFormatVC(vc);
            NodeList attrNodes = (NodeList) xmlPathExp.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < attrNodes.getLength(); i++) {
                Node attrNode = attrNodes.item(i);
                if (attrNode.getTextContent().equals(uuidVc)) {
                    removeChildren(attrNode);
                }
            }
        }
        return Optional.ofNullable(doc);
    }

    private void removeChildren(Node attrNode) {
        Attr attr = (Attr) attrNode;
        Node nodeOwner = attr.getOwnerElement();
        if (nodeOwner.getParentNode() != null) {
            nodeOwner.getParentNode().removeChild(nodeOwner);
        }
    }

    public void printAllVC(Document doc) throws XPathExpressionException {
        if (doc != null) {
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

    public Optional<Document> getRelsExt(String uuid) {
        return getDataStream(uuid, DataStreams.RELS_EXT.name);
    }

    private Optional<Document> getDataStream(String uuid, String dsName) {
        return getFedoraResource(fedoraHost + "/get/" + uuid + "/" + dsName);
    }

    private Optional<Document> getFedoraResource(String url) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            String str = Objects.requireNonNull(response.getBody());
            return Optional.of(xmlParser.parse(new InputSource(new StringReader(str))));
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        } catch (HttpClientErrorException e) {
            e.getMessage();
        }
        return Optional.empty();
    }

    public void setRelsExt(String uuid, Document relsExt) throws TransformerException {
        if (relsExt != null) {
            Optional<HttpEntity<Object>> entity = docStrEntity(relsExt, DataStreams.RELS_EXT.mimeType);
            entity.ifPresent(objectHttpEntity -> setDataStream(uuid, DataStreams.RELS_EXT, objectHttpEntity));
        }
    }

    private Optional<HttpEntity<Object>> docStrEntity(Document doc, String mimeType) throws TransformerException {
        if (doc != null) {
            return Optional.of(new HttpEntity<>(docToStr(doc), createMimeTypeHeaders(mimeType)));
        }
        return Optional.empty();
    }

    private HttpHeaders createMimeTypeHeaders(String mimeType) {
        HttpHeaders headers = new HttpHeaders(httpHeaders);
        headers.setContentType(MediaType.parseMediaType(mimeType));
        return headers;
    }

    private String docToStr(Document doc) throws TransformerException {
        StreamResult streamResult = new StreamResult(new StringWriter());
        DOMSource domSource = new DOMSource(doc);
        xmlTransformer.transform(domSource, streamResult);
        return streamResult.getWriter().toString();
    }

    private String getFullFormatVC(String vc) {
        return "info:fedora/" + vc;
    }

    private void setDataStream(String uuid, DataStreams ds, HttpEntity<Object> entity) {
        String url = fedoraHost + "/objects/" + uuid + "/datastreams/" + ds.name;

        Map<String, String> uriParam = new HashMap<>() {{
            put("mimeType", ds.mimeType);
            put("versionable", ds.versionable);
            put("controlGroup", ds.controlGroup);
            put("state", ds.state);
        }};
        url = ClientApiConfig.buildUri(url, uriParam);

        restTemplate.postForEntity(url, entity, String.class);
    }

}
