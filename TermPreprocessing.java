import org.tartarus.snowball.ext.PorterStemmer;

public class TermPreprocessing {
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

    public static String deleteSpaces(String term) {
        return term.replaceAll(" ", "").replaceAll("\n", " ");
    }
}
