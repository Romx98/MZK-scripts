package cz.mzk.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;

@Slf4j
public class FoxmlUtils {

    private final XPathExpression isMemberOfCollectionXPath;

    public FoxmlUtils() {
        final XPathFactory xFactory = XPathFactory.newInstance();
        final XPath xmlPath = xFactory.newXPath();
        isMemberOfCollectionXPath = compile("//*[local-name() = 'isMemberOfCollection']/@resource", xmlPath);
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
