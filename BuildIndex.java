import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class BuildIndex {
    public static final String TITLE_XPATH = "TEI/teiHeader/fileDesc/titleStmt/title";
    public static final String AUTHOR_NAME_XPATH = "TEI/teiHeader/fileDesc/sourceDesc/biblStruct/analytic/author/descendant::persName";
    public static final String AUTHOR_EMAIL_XPATH = "TEI/teiHeader/fileDesc/sourceDesc/biblStruct/analytic/author/descendant::email";
    public static final String AFFILIATION_XPATH = "TEI/teiHeader/fileDesc/sourceDesc/biblStruct/analytic/author/descendant::orgName";
    public static final String ADDRESS_XPATH = "TEI/teiHeader/fileDesc/sourceDesc/biblStruct/analytic/author/descendant::address";
    public static final String KEYWORDS_XPATH = "TEI/teiHeader/profileDesc/textClass/keywords/descendant::term";
    public static final String ABSTRACT_XPATH = "TEI/teiHeader/profileDesc/abstract/descendant::div";
    public static final String FULLTEXT_XPATH = "TEI/text/body/descendant::div";
    public static final String FIGURE_DESC_XPATH = "TEI/text/body/descendant::figDesc";
    public static final String REFERENCE_TITLE_XPATH = "TEI/text/back/div[2]/listBibl/descendant::title";
    public static final String REFERENCE_AUTHOR_XPATH = "TEI/text/back/div[2]/listBibl/descendant::persName";

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        Directory directory = FSDirectory.open(Paths.get("index"));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        // TODO: 注意没有索引 date 字段

        File file = new File("/Users/chenxuan/Desktop/大三/IR/ori-pdf-out");
        // File file1 = new File("/Users/chenxuan/IdeaProjects/IR/Oct-13-2/test");
        File[] fileList = file.listFiles();
        int countHelper = 0;
        assert fileList != null;
        for (var f : fileList) {
            if (!f.toString().endsWith(".xml")) {
                continue;
            }
            System.out.println(++countHelper);
            Document XMLDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
            org.apache.lucene.document.Document luceneDocument = new org.apache.lucene.document.Document();
            XPath xPath = XPathFactory.newInstance().newXPath(); // 创建 xpath 对象

            // 标题 title
            // TODO: 这里我直接用了 Porter 算法
            try {
                String titleString = TermPreprocessing.getStemmedTerms(xPath.evaluate(TITLE_XPATH, XMLDocument).trim());
                System.out.println(titleString);
                Field titleField = new TextField("title", titleString, Field.Store.YES);
                luceneDocument.add(titleField);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // 作者 author
            try {
                NodeList authorNameNodes = (NodeList) xPath.evaluate(AUTHOR_NAME_XPATH, XMLDocument, XPathConstants.NODESET);
                int authorNameNodesLength = authorNameNodes.getLength();
                StringBuilder authorNameStringBuilder = new StringBuilder(64);
                for (int i = 0; i < authorNameNodesLength; ++i) {
                    NodeList currentAuthorName = authorNameNodes.item(i).getChildNodes();
                    int authorNameStructureIndicator = currentAuthorName.getLength();
                    for (int j = 0; j < authorNameStructureIndicator; ++j) {
                        String currentAuthorNamePart = currentAuthorName.item(j).getTextContent().trim();
                        if (!currentAuthorNamePart.isEmpty()) {
                            authorNameStringBuilder.append(TermPreprocessing.deleteSpaces(currentAuthorNamePart)).append(" ");
                        }
                    }
                }
                // 已修复: 注意这里没有将作者的姓与名之间加上空格
                String authorString = authorNameStringBuilder.toString();
                System.out.println(authorString);
                Field authorField = new TextField("author", authorString, Field.Store.YES);
                luceneDocument.add(authorField);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // 邮箱 email
            try {
                NodeList authorEmailNodes = (NodeList) xPath.evaluate(AUTHOR_EMAIL_XPATH, XMLDocument, XPathConstants.NODESET);
                int authorEmailNodesLength = authorEmailNodes.getLength();
                StringBuilder authorEmailStringBuilder = new StringBuilder(64);
                for (int i = 0; i < authorEmailNodesLength; ++i) {
                    authorEmailStringBuilder.append(authorEmailNodes.item(i).getTextContent().trim()).append(" ");
                }
                String emailString = authorEmailStringBuilder.toString();
                System.out.println(emailString);
                Field emailField = new TextField("email", emailString, Field.Store.YES);
                luceneDocument.add(emailField);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // 机构 affiliation
            try {
                NodeList affiliationNodes = (NodeList) xPath.evaluate(AFFILIATION_XPATH, XMLDocument, XPathConstants.NODESET);
                int affiliationNodesLength = affiliationNodes.getLength();
                StringBuilder affiliationStringBuilder = new StringBuilder(256);
                for (int i = 0; i < affiliationNodesLength; ++i) {
                    affiliationStringBuilder.append(affiliationNodes.item(i).getTextContent().trim()).append(" ");
                }
                String affiliationString = affiliationStringBuilder.toString();
                System.out.println(affiliationString);
                Field affiliationField = new TextField("affiliation", affiliationString, Field.Store.YES);
                luceneDocument.add(affiliationField);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // 地址 address
            try {
                NodeList addressNodes = (NodeList) xPath.evaluate(ADDRESS_XPATH, XMLDocument, XPathConstants.NODESET);
                int addressNodesLength = addressNodes.getLength();
                StringBuilder addressStringBuilder = new StringBuilder(128);
                for (int i = 0; i < addressNodesLength; ++i) {
                    addressStringBuilder.append(TermPreprocessing.deleteSpaces(addressNodes.item(i).getTextContent().trim())).append(" ");
                }
                String addressString = addressStringBuilder.toString();
                System.out.println(addressString);
                Field addressField = new TextField("address", addressString, Field.Store.YES);
                luceneDocument.add(addressField);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // 关键词 keywords Porter
            // TODO: 这里我直接用了 Porter 算法
            try {
                NodeList keyWordsNodes = (NodeList) xPath.evaluate(KEYWORDS_XPATH, XMLDocument, XPathConstants.NODESET);
                int keyWordsNodesLength = keyWordsNodes.getLength();
                StringBuilder keyWordsStringBuilder = new StringBuilder(128);
                for (int i = 0; i < keyWordsNodesLength; ++i) {
                    keyWordsStringBuilder.append(keyWordsNodes.item(i).getTextContent().trim()).append(" ");
                }
                String keywordsString = TermPreprocessing.getStemmedTerms(keyWordsStringBuilder.toString());
                System.out.println(keywordsString);
                Field keywordsField = new TextField("keywords", keywordsString, Field.Store.YES);
                luceneDocument.add(keywordsField);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // 摘要 abstract
            // TODO: 这里我直接用了 Porter 算法
            try {
                String abstractString = TermPreprocessing.getStemmedTerms(xPath.evaluate(ABSTRACT_XPATH, XMLDocument).trim());
                System.out.println(abstractString);
                Field abstractField = new TextField("abstract", abstractString, Field.Store.YES);
                luceneDocument.add(abstractField);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // 正文 fulltext
            // TODO: 这里我直接用了 Porter 算法
            try {
                NodeList fulltextNodes = (NodeList) xPath.evaluate(FULLTEXT_XPATH, XMLDocument, XPathConstants.NODESET);
                int fulltextNodesLength = fulltextNodes.getLength();
                StringBuilder fulltextStringBuilder = new StringBuilder(65536);
                for (int i = 0; i < fulltextNodesLength; ++i) {
                    fulltextStringBuilder.append(fulltextNodes.item(i).getTextContent().trim().replaceAll("\t", " ").replaceAll("\n", " ")).append(" ");
                }
                String fulltextString = TermPreprocessing.getStemmedTerms(fulltextStringBuilder.toString());
                System.out.println(fulltextString);
                Field fulltextField = new TextField("fulltext", fulltextString, Field.Store.YES);
                luceneDocument.add(fulltextField);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // 图片说明 figure
            // TODO: 这里我直接用了 Porter 算法
            try {
                NodeList figureNodes = (NodeList) xPath.evaluate(FIGURE_DESC_XPATH, XMLDocument, XPathConstants.NODESET);
                int figureNodesLength = figureNodes.getLength();
                StringBuilder figureStringBuilder = new StringBuilder(2048);
                for (int i = 0; i < figureNodesLength; ++i) {
                    figureStringBuilder.append(figureNodes.item(i).getTextContent().trim().replaceAll("\t", " ").replaceAll("\n", " ")).append(" ");
                }
                String figureString = TermPreprocessing.getStemmedTerms(figureStringBuilder.toString());
                System.out.println(figureString);
                Field figureField = new TextField("figure", figureString, Field.Store.YES);
                luceneDocument.add(figureField);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // 被引文献 ref_title
            try {
                NodeList referenceTitleNodes = (NodeList) xPath.evaluate(REFERENCE_TITLE_XPATH, XMLDocument, XPathConstants.NODESET);
                int referenceTitleNodesLength = referenceTitleNodes.getLength();
                StringBuilder referenceTitleStringBuilder = new StringBuilder(4096);
                for (int i = 0; i < referenceTitleNodesLength; ++i) {
                    referenceTitleStringBuilder.append(referenceTitleNodes.item(i).getTextContent().trim().replaceAll("\t", " ").replaceAll("\n", " ")).append(" ");
                }
                // FIXME: 注意这里没有区分被引文献的标题和所在的期刊
                String referenceTitleString = referenceTitleStringBuilder.toString();
                System.out.println(referenceTitleString);
                Field referenceTitleField = new TextField("ref_title", referenceTitleString, Field.Store.YES);
                luceneDocument.add(referenceTitleField);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // 被引作者 ref_author
            try {
                NodeList referenceAuthorNodes = (NodeList) xPath.evaluate(REFERENCE_AUTHOR_XPATH, XMLDocument, XPathConstants.NODESET);
                int referenceAuthorNodesLength = referenceAuthorNodes.getLength();
                StringBuilder referenceAuthorStringBuilder = new StringBuilder(128);
                for (int i = 0; i < referenceAuthorNodesLength; ++i) {
                    NodeList currentReferenceAuthorName = referenceAuthorNodes.item(i).getChildNodes();
                    int referenceAuthorNameStructureIndicator = currentReferenceAuthorName.getLength();
                    for (int j = 0; j < referenceAuthorNameStructureIndicator; ++j) {
                        String currentReferenceAuthorNamePart = currentReferenceAuthorName.item(j).getTextContent().trim();
                        if (!currentReferenceAuthorNamePart.isEmpty()) {
                            referenceAuthorStringBuilder.append(TermPreprocessing.deleteSpaces(currentReferenceAuthorNamePart)).append(" ");
                        }
                    }
                    referenceAuthorStringBuilder.append(TermPreprocessing.deleteSpaces(referenceAuthorNodes.item(i).getTextContent().trim()).replaceAll("\t", " ")).append(" ");
                }
                String referenceAuthorString = referenceAuthorStringBuilder.toString();
                System.out.println(referenceAuthorString);
                Field referenceAuthorField = new TextField("ref_author", referenceAuthorString, Field.Store.YES);
                luceneDocument.add(referenceAuthorField);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            indexWriter.addDocument(luceneDocument);
        }
        indexWriter.close();
    }
}
