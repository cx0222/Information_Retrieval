import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.nio.file.Paths;

public class SearchTest {
    public static final String EXPRESSION = "forests";
    // FIXME: 这里应该要写一个方法

    public static final String[] MULTI_FIELDS = new String[]{"title"};

    public static String getStemmedTerms(String terms) {
        StringBuilder res = new StringBuilder();
        String[] s = terms.split(" ");
        for (String term : s) {
            res.append(getStemmedTerm(term)).append(" ");
        }
        return res.toString();
    }

    public static String getStemmedTerm(String term) {
        PorterStemmer porterStemmer = new PorterStemmer();
        porterStemmer.setCurrent(term.toLowerCase());
        porterStemmer.stem();
        return porterStemmer.getCurrent();
    }

    public static void main(String[] args) throws IOException, ParseException {
        FSDirectory fsDirectory = FSDirectory.open(Paths.get("/Users/chenxuan/IdeaProjects/IR/Oct-13-2/index"));
        IndexReader indexReader = DirectoryReader.open(fsDirectory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
        MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(MULTI_FIELDS, standardAnalyzer);
        Query query = multiFieldQueryParser.parse(getStemmedTerms(EXPRESSION));
        // System.out.println(query.toString());
        TopDocs topDocs = indexSearcher.search(query, 20);

        for (ScoreDoc scoreDoc: topDocs.scoreDocs) {
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.println(scoreDoc.doc);
            System.out.println(document.get("id"));
            System.out.println(document.get("title"));
            System.out.println(scoreDoc.score);
        }
    }
}
