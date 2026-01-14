package com.one.range.controller.basevul.xxe;

import com.one.range.util.Security;
import org.dom4j.io.SAXReader;
import org.jdom2.input.SAXBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

@RestController
@RequestMapping("/home/xxe")
public class XXE {

    /**
     * @poc http://127.0.0.1:8888/XXE/XMLReader （POC）
     * Content-Type: application/xml
     * payload: <?xml version="1.0" encoding="utf-8"?><!DOCTYPE test [<!ENTITY xxe SYSTEM "http://0g5zvd.dnslog.cn">]><root>&xxe;</root>
     */
    @PostMapping(value = "/xmlreader")
    public String XMLReader(@RequestBody String content, String entity) {
        try {
            content = normalizeXml(content);
            if (entity !=null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            // 将功能 "http://apache.org/xml/features/disallow-doctype-decl" 设置为"真"时, 不允许使用 DOCTYPE。
            // xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            
            // 使用ContentHandler来捕获解析的内容
            StringBuilder result = new StringBuilder();
            ContentHandler handler = new ContentHandler() {
                @Override
                public void setDocumentLocator(org.xml.sax.Locator locator) {}
                
                @Override
                public void startDocument() {}
                
                @Override
                public void endDocument() {}
                
                @Override
                public void startPrefixMapping(String prefix, String uri) {}
                
                @Override
                public void endPrefixMapping(String prefix) {}
                
                @Override
                public void startElement(String uri, String localName, String qName, Attributes atts) {}
                
                @Override
                public void endElement(String uri, String localName, String qName) {}
                
                @Override
                public void characters(char[] ch, int start, int length) {
                    result.append(new String(ch, start, length));
                }
                
                @Override
                public void ignorableWhitespace(char[] ch, int start, int length) {}
                
                @Override
                public void processingInstruction(String target, String data) {}
                
                @Override
                public void skippedEntity(String name) {}
            };
            
            xmlReader.setContentHandler(handler);
            xmlReader.parse(new InputSource(new StringReader(content)));
            
            String parsedContent = result.toString().trim();
            return parsedContent.isEmpty() ? "XMLReader 解析成功，但未找到文本内容" : "解析结果: " + parsedContent;
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }


    @PostMapping(value = "/saxreader")
    public String SAXReader(@RequestBody String content, String entity) {
        try {
            content = normalizeXml(content);
            if (entity !=null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }
            SAXReader sax = new SAXReader();
            // 修复：禁用外部实体
            // sax.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            org.dom4j.Document document = sax.read(new InputSource(new StringReader(content)));
            // 返回文档的文本内容
            String text = document.getRootElement().getText();
            return text.isEmpty() ? "SAXReader 解析成功，但未找到文本内容" : "解析结果: " + text;
        } catch (Exception e) {
            return "错误: " + e.toString();
        }

    }


    // SAXBuilder是一个JDOM解析器，能将路径中的XML文件解析为Document对象
    @PostMapping(value = "/saxbuilder")
    public String SAXBuilder(@RequestBody String content, String entity) {
        try {
            content = normalizeXml(content);
            if (entity !=null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }
            SAXBuilder saxbuilder = new SAXBuilder();
            // saxbuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            org.jdom2.Document document = saxbuilder.build(new InputSource(new StringReader(content)));
            // 返回文档的文本内容
            org.jdom2.Element root = document.getRootElement();
            String text = root.getText();
            return text.isEmpty() ? "SAXBuilder 解析成功，但未找到文本内容" : "解析结果: " + text;
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }


    /**
     * @poc http://127.0.0.1:8888/XXE/DocumentBuilder
     * payload: <?xml version="1.0" encoding="utf-8"?><!DOCTYPE test [<!ENTITY xxe SYSTEM "file:///etc/passwd">]><person><name>&xxe;</name></person>
     */
    @PostMapping(value = "/documentbuilder")
    public String DocumentBuilder(@RequestBody String content, String entity) {
        try {
            content = normalizeXml(content);
            if (entity !=null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }
            // DocumentBuilderFactory是用于创建DOM模式的解析器对象,newInstance方法会根据本地平台默认安装的解析器，自动创建一个工厂的对象并返回。
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            StringReader sr = new StringReader(content);
            InputSource is = new InputSource(sr);
            Document document = builder.parse(is);

            // 尝试获取根元素的文本内容
            Element rootElement = document.getDocumentElement();
            if (rootElement != null) {
                String rootTagName = rootElement.getTagName();
                NodeList nodeList = document.getElementsByTagName(rootTagName);
                if (nodeList.getLength() > 0) {
                    Element element = (Element) nodeList.item(0);
                    if (element.getFirstChild() != null) {
                        String textContent = element.getTextContent();
                        return textContent.isEmpty() ? "DocumentBuilder 解析成功，但未找到文本内容" : "解析结果: " + textContent;
                    }
                }
            }
            
            // 如果找不到person标签，尝试获取根元素的文本内容
            String textContent = document.getDocumentElement().getTextContent();
            return textContent.isEmpty() ? "DocumentBuilder 解析成功，但未找到文本内容" : "解析结果: " + textContent;

        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }


    /**
     * @poc http://127.0.0.1:8888/XXE/unmarshaller (POST)
     * body payload <?xml version="1.0" encoding="UTF-8"?><!DOCTYPE student[<!ENTITY out SYSTEM "file:///etc/passwd">]><student><name>&out;</name></student>
     */
    @PostMapping(value = "/unmarshaller")
    public String Unmarshaller(@RequestBody String content, String entity) {
        try {
            content = normalizeXml(content);
            if (entity !=null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }
            JAXBContext context = JAXBContext.newInstance(Student.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            XMLInputFactory xif = XMLInputFactory.newFactory();
            // fixed: 禁用外部实体
            // xif.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            // xif.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

            // 默认情况下在1.8版本上不能加载外部dtd文件，需要更改设置。
            // xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, true);
            // xif.setProperty(XMLInputFactory.SUPPORT_DTD, true);
            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(content));

            Object o = unmarshaller.unmarshal(xsr);

            return o != null ? o.toString() : "Unmarshaller 解析成功，但返回值为空";

        } catch (Exception e) {
            return "错误: " + e.toString();
        }

    }


    @RequestMapping(value = "/safe")
    public String check(@RequestBody String content) {
        if (!Security.checkXXE(content)) {
            return "safe";
        } else {
            return "检测到XXE攻击";
        }
    }

    /**
     * 去掉前导空白和XML声明，避免部分解析器因XML声明位置严格校验抛出
     * “processing instruction target matching \"[xX][mM][lL]\" is not allowed”。
     */
    private String normalizeXml(String content) {
        if (content == null) {
            return "";
        }
        String trimmed = content.trim();
        return trimmed.replaceFirst("^<\\?xml[^>]*>", "");
    }

}
