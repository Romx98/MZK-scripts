package cz.mzk.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class FoxmlUtils {

    private final XPathExpression isMemberOfCollectionXPath;
    private final XPathExpression identifierXPath;

    public FoxmlUtils() {
        final XPathFactory xFactory = XPathFactory.newInstance();
        final XPath xmlPath = xFactory.newXPath();
        isMemberOfCollectionXPath = compile("//*[local-name() = 'isMemberOfCollection']/@resource", xmlPath);
        identifierXPath = compile("//*[local-name() = 'identifier']/text()", xmlPath);
    }

    public List<String> getListOfCNBFromFoxml(final Document doc) {
        Validate.notNull(doc);

        return parseIdentifierByValueName(doc, "cnb");
    }

    public Optional<String> getStrISSNFromFoxml(final Document doc) {
        Validate.notNull(doc);

        final List<String> listOfISSN = parseIdentifierByValueName(doc, "issn");
        if (!listOfISSN.isEmpty()) {
            return Optional.of(listOfISSN.get(0));
        }
        return Optional.empty();
    }

    private List<String> parseIdentifierByValueName(final Document doc, final String valueName) {
        Validate.notNull(doc);
        Validate.notBlank(valueName);

        try {
            final NodeList attrNodes = (NodeList) identifierXPath.evaluate(doc, XPathConstants.NODESET);
            final List<String> values = new ArrayList<>();

            for (int i = 0; i < attrNodes.getLength(); i++) {
                final Node attrNode = attrNodes.item(i);
                if (attrNode.getTextContent().contains(valueName)) {
                    values.add(attrNode.getTextContent());
                }
            }
            return values;
        } catch (XPathExpressionException e) {
            log.warn("Can't retrieve identifier \"" + valueName + "\"");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public boolean removeVc(final Document doc, final String vcUuid) {
        Validate.notNull(doc);
        Validate.notBlank(vcUuid);

        try {
            boolean updated = false;
            final String fedoraFormattedVcUuid = getFedoraFormattedUuid(vcUuid);
            final NodeList attrNodes = (NodeList) isMemberOfCollectionXPath.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < attrNodes.getLength(); i++) {
                Node attrNode = attrNodes.item(i);
                if (attrNode.getTextContent().equals(fedoraFormattedVcUuid)) {
                    removeChildren(attrNode);
                    updated = true;
                }
            }
            return updated;
        } catch (XPathExpressionException e) {
            log.warn("Can't retrieve and remove members of collection \"" + vcUuid + "\"!");
            e.printStackTrace();
            return false;
        }
    }

    private void removeChildren(final Node attrNode) {
        Validate.notNull(attrNode);

        final Attr attr = (Attr) attrNode;
        final Node nodeOwner = attr.getOwnerElement();
        if (nodeOwner.getParentNode() != null) {
            nodeOwner.getParentNode().removeChild(nodeOwner);
        }
    }

    private String getFedoraFormattedUuid(final String uuid) {
        Validate.notBlank(uuid);

        return "info:fedora/" + uuid;
    }

    private XPathExpression compile(final String expr, final XPath xPath) {
        Validate.notBlank(expr);
        Validate.notNull(xPath);

        try {
            return xPath.compile(expr);
        } catch (XPathExpressionException e) {
            log.warn("Can't compile XPath expressions \"" + expr + "\"! Return null object!");
            e.printStackTrace();
            return null;
        }
    }
}
