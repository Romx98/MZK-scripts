package cz.mzk.rest;

import cz.mzk.constants.DataStreams;
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

    public FedoraRestClient(final String fh, final String fu, final String fp) throws ParserConfigurationException, TransformerException {
        fedoraHost = fh;
        restTemplate = getConfiguredRestTemplate();
        httpHeaders = createHttpHeaders(fu, fp);
        httpEntity = new HttpEntity<>(httpHeaders);

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //factory.setNamespaceAware(true);
        xmlParser = factory.newDocumentBuilder();

        final TransformerFactory tFactory = TransformerFactory.newInstance();
        xmlTransformer = tFactory.newTransformer();
    }

    public Optional<Document> getFoxmlByUuid(final String uuid) {
        return getFedoraResource(fedoraHost + "/objects/" + uuid + "/objectXML");
    }

    public Optional<Document> getDC(final String uuid) {
        return getDataStream(uuid,DataStreams.DC.name);
    }

    public Optional<Document> getRelsExt(final String uuid) {
        return getDataStream(uuid, DataStreams.RELS_EXT.name);
    }

    private Optional<Document> getDataStream(final String uuid, final String dsName) {
        return getFedoraResource(fedoraHost + "/get/" + uuid + "/" + dsName);
    }

    private Optional<Document> getFedoraResource(final String url) {
        try {
            final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            final String str = Objects.requireNonNull(response.getBody());
            return Optional.of(xmlParser.parse(new InputSource(new StringReader(str))));
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        } catch (HttpClientErrorException e) {
            e.getMessage();
        }
        return Optional.empty();
    }

    private HttpHeaders createHttpHeaders(final String fu, final String fp) {
        final String credentials = fu + ":" + fp;
        final String encodeCredentials = new String(Base64.encodeBase64(credentials.getBytes()));
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + encodeCredentials);
        return headers;
    }

    public void setRelsExt(final String uuid, final Document relsExt) throws TransformerException {
        if (relsExt != null) {
            final Optional<HttpEntity<Object>> entity = docStrEntity(relsExt, DataStreams.RELS_EXT.mimeType);
            entity.ifPresent(objectHttpEntity -> setDataStream(uuid, DataStreams.RELS_EXT, objectHttpEntity));
        }
    }

    private Optional<HttpEntity<Object>> docStrEntity(final Document doc, final String mimeType) throws TransformerException {
        if (doc != null) {
            return Optional.of(new HttpEntity<>(docToStr(doc), createMimeTypeHeaders(mimeType)));
        }
        return Optional.empty();
    }

    private HttpHeaders createMimeTypeHeaders(final String mimeType) {
        final HttpHeaders headers = new HttpHeaders(httpHeaders);
        headers.setContentType(MediaType.parseMediaType(mimeType));
        return headers;
    }

    private String docToStr(final Document doc) throws TransformerException {
        final StreamResult streamResult = new StreamResult(new StringWriter());
        final DOMSource domSource = new DOMSource(doc);
        xmlTransformer.transform(domSource, streamResult);
        return streamResult.getWriter().toString();
    }

    private void setDataStream(final String uuid, final DataStreams ds, final HttpEntity<Object> entity) {
        String url = fedoraHost + "/objects/" + uuid + "/datastreams/" + ds.name;

        final Map<String, String> uriParam = new HashMap<>() {{
            put("mimeType", ds.mimeType);
            put("versionable", ds.versionable);
            put("controlGroup", ds.controlGroup);
            put("state", ds.state);
        }};
        url = buildUri(url, uriParam);

        restTemplate.postForEntity(url, entity, String.class);
    }

    private String buildUri(final String url, final Map<String, ?> params) {
        final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        for (final Map.Entry<String, ?> entry : params.entrySet()) {
            builder.queryParam(entry.getKey(), entry.getValue());
        }
        return builder.encode().build().toUri().toString();
    }

    private RestTemplate getConfiguredRestTemplate() {
        final RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new PlusEncoderInterceptor()));
        return restTemplate;
    }
}
