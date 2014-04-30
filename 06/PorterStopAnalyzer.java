import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

//LauraRettig_TP6a7

public class PorterStopAnalyzer extends StopwordAnalyzerBase {
	
	private static final CharArraySet STOP_WORDS = new CharArraySet(Version.LUCENE_47, getStopWords(), false);

	public PorterStopAnalyzer(Version matchVersion) {
		this(matchVersion, STOP_WORDS);
	}
	
	public PorterStopAnalyzer(Version matchVersion, CharArraySet stopwords) {
		super(matchVersion, stopwords);
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		final Tokenizer source = new LowerCaseTokenizer(matchVersion, reader);
		return new TokenStreamComponents(source, new PorterStemFilter(new StopFilter(matchVersion, source, stopwords)));
	}
	
	public static List<String> getStopWords() {
		List<String> stopw = new ArrayList();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("docs/cacm/common_words")));
			String word;
			while ((word = br.readLine()) != null) {
				stopw.add(word);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stopw;
	}

}
