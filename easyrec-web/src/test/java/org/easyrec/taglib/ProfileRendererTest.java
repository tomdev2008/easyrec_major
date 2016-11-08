package org.easyrec.taglib;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for simple App.
 */
@RunWith(UnitilsJUnit4TestClassRunner.class)
public class ProfileRendererTest {

    protected ProfileRenderer profileRenderer = new ProfileRenderer();
    private final Log logger = LogFactory.getLog(this.getClass());
    private String singleLayerProfileXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><profile><description>Description stored as a profile. Plus an additional sentence.</description><name>profileItem</name></profile>";
    private String multiLayerProfileXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><profile><description>Description stored as a profile. Plus an additional sentence.</description><genre>ROCK</genre><genre>POP</genre></profile>";
    private String multiLayerDeepProfileXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><profile><description>Description stored as a profile. Plus an additional sentence.</description><genres><genre>ROCK</genre><genre>POP</genre></genres></profile>";


    @Test
    public void singleLayer() throws IOException, SAXException, ParserConfigurationException {
        String profileHTML = profileRenderer.getListViewHTML(singleLayerProfileXML);
        assertEquals("<dl><dt>description:</dt><dd>Description stored as a profile. Plus an additional sentence.</dd><dt>name:</dt><dd>profileItem</dd></dl>", profileHTML);
    }

    @Test
    public void multiLayer() throws IOException, SAXException, ParserConfigurationException {
        String profileHTML = profileRenderer.getListViewHTML(multiLayerProfileXML);
        assertEquals("<dl><dt>description:</dt><dd>Description stored as a profile. Plus an additional sentence.</dd><dt>genre:</dt><dd>ROCK</dd><dt>genre:</dt><dd>POP</dd></dl>", profileHTML);
    }

    @Test
    public void multiLayerDeep() throws IOException, SAXException, ParserConfigurationException {
        String profileHTML = profileRenderer.getListViewHTML(multiLayerDeepProfileXML);
        assertEquals("<dl><dt>description:</dt><dd>Description stored as a profile. Plus an additional sentence.</dd><dt>genres:</dt><dd><dl><dt>genre:</dt><dd>ROCK</dd><dt>genre:</dt><dd>POP</dd></dl></dd></dl>", profileHTML);
    }
}
