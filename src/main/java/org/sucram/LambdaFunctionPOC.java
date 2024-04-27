package org.sucram;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class LambdaFunctionPOC implements RequestHandler<Object, String> {

    private static final String S3_BUCKET_NAME = "demobucket4963";
    private static final String XML_CONTENT = "<data><item>Example</item></data>";
    private static final String XSLT_CONTENT = "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
            + "<xsl:template match=\"/data\"><root><xsl:apply-templates/></root></xsl:template>"
            + "<xsl:template match=\"item\"><item><xsl:value-of select=\".\"/></item></xsl:template>"
            + "</xsl:stylesheet>";

    @Override
    public String handleRequest(Object input, Context context) {
        try {
            // Generate XML content
            String xmlContent = generateXML();

            // Transform XML using XSLT
            String transformedXml = transformXML(xmlContent);

            // Upload transformed XML to S3
            uploadToS3(transformedXml, "transformed.xml");

            return "Success";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String generateXML() {
        // You can generate your XML content here dynamically
        return XML_CONTENT;
    }

    private String transformXML(String xmlContent) throws Exception {
        // Perform XSLT transformation
        TransformerFactory factory = TransformerFactory.newInstance();
        InputStream xsltStream = new ByteArrayInputStream(XSLT_CONTENT.getBytes(StandardCharsets.UTF_8));
        Transformer transformer = factory.newTransformer(new StreamSource(xsltStream));

        InputStream xmlStream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        transformer.transform(new StreamSource(xmlStream), new StreamResult(outputStream));

        return outputStream.toString(StandardCharsets.UTF_8.name());
    }

    private void uploadToS3(String content, String key) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(content.getBytes().length);
        s3Client.putObject(new PutObjectRequest(S3_BUCKET_NAME, key, inputStream, metadata));
    }
}
