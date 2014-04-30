import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;

/*
	CACM corpus indexing
	applying Porter stemmer OR simple S-stemmer and StopAnalyzer
*/

public class Exercise7 {

	public enum Mode {
		TITLE, KEYS, ABSTRACT
	}

	public static void main(String[] args) throws ParseException, IOException {
		
		// 1) Indexing
		
		// choose one of the two own defined analyzers for task 3 and 4 respectively
		//Analyzer analyzer = new PorterStopAnalyzer(Version.LUCENE_47);
		Analyzer analyzer = new SStopAnalyzer(Version.LUCENE_47);
		Directory indexDirectory = new RAMDirectory();
		IndexWriter indexWriter = new IndexWriter(indexDirectory, new IndexWriterConfig(Version.LUCENE_47, analyzer)); 
		
		indexCorpus(indexWriter, "docs/cacm/cacm.all");
		
		indexWriter.close();
		
		// 2) Searching
		
		try {
			searchNQueries(10, 10, analyzer, indexDirectory);
		} catch (org.apache.lucene.queryparser.classic.ParseException e) {
			e.printStackTrace();
		}
		
		
		indexDirectory.close();
		
	}
	
	
	static Document document = null;
	private static void indexCorpus(IndexWriter w, String location) throws IOException {
		// index the documents in the cacm.all corpus
		File all = new File(location);
		BufferedReader br = new BufferedReader(new FileReader(all));
		int totaldocs = 0;
		int emptydocs = 0;
		int hasFields = 0;
		String line;
		Mode mode = null;
		String title = "";
		String keywords = "";
		String abs = "";
		while ((line = br.readLine()) != null) {
			if (line.matches("\\.I.*")) {
				hasFields += modeHandler(mode, title, keywords, abs);
				if (document != null) {
					w.addDocument(document); 		// previous document is complete, start new one
					totaldocs++;
					if (hasFields == 0) emptydocs++;
				}
				document = new Document();
				hasFields = 0;
				String id = line.substring(3);
				document.add(new StringField("id", id, Field.Store.YES));
			}
			else if (line.matches("\\.T")) {
				hasFields += modeHandler(mode, title, keywords, abs);
				title = "";
				mode = Mode.TITLE;
			}
			else if (line.matches("\\.K")) {
				hasFields += modeHandler( mode, title, keywords, abs);
				keywords = "";
				mode = Mode.KEYS;
			}
			else if (line.matches("\\.W")) {
				hasFields += modeHandler( mode, title, keywords, abs);
				abs = "";
				mode = Mode.ABSTRACT;
			}
			else if (line.matches("\\..*")) {
				hasFields += modeHandler( mode, title, keywords, abs);
				mode = null;
			}
			else if (mode == Mode.TITLE) {
				title = title + " " + line;				
			}
			else if (mode == Mode.KEYS) {
				keywords = keywords + " " + line;			
			}
			else if (mode == Mode.ABSTRACT) {
				abs = abs + " " + line;				
			}
		}
		br.close();
	}
	
	private static int modeHandler( Mode mode, String title, String keywords, String abs) {
		if (mode != null) {
			switch (mode) {
				case TITLE:
					document.add(new TextField("title", title, Field.Store.YES));
					return 1;
				case KEYS:
					document.add(new TextField("keywords", keywords, Field.Store.YES));
					return 1;
				case ABSTRACT:
					document.add(new TextField("abstract", abs, Field.Store.YES));
					return 1;
			}
		}
		return 0;
	}
	
	private static void searchNQueries(int n, int nres, Analyzer analyzer, Directory dir) throws org.apache.lucene.queryparser.classic.ParseException, IOException {
		// get first n queries
		String[] queries = extractQueries(n);
		String[] fields = {"title","keywords","abstract"};

		IndexReader reader = DirectoryReader.open(dir); 
		IndexSearcher searcher = new IndexSearcher(reader);
		
		// search and print results
		for (String query : queries) {
			Query q = new MultiFieldQueryParser(Version.LUCENE_47, fields, analyzer).parse(query);
			TopScoreDocCollector res = TopScoreDocCollector.create(nres, true);
			searcher.search(q, res);
			ScoreDoc[] hits = res.topDocs().scoreDocs;
			System.out.println("\nTop " + nres + " documents, ranked for the query \"" + query + "\":");
			int i = 1;
			for (ScoreDoc hit : hits) {
				System.out.println(i + ". ID: " + searcher.doc(hit.doc).getValues("id")[0] + ", title: " + searcher.doc(hit.doc).getValues("title")[0]/* + " score: " + hit.score*/);
				i++;
			}
		}
		reader.close();
	}
	
	private static String[] extractQueries(int n) throws IOException {
		File qtext = new File("docs/cacm/query.text");
		BufferedReader br = new BufferedReader(new FileReader(qtext));
		String[] queries = new String[n];
		String line;
		int i = 0;
		Boolean mode = false;
		String query = null;
		while ((line = br.readLine()) != null) {
			if (line.matches("\\.I.*")) {
				if (query != null) { 		// ignore at first appearance of .I
					queries[i] = query;
					i++;
				}
				query = "";
				if (i == n) break;
			}
			else if (line.matches("\\.W")) {
				mode = true;
			}
			else if (line.matches("\\..*")) {
				mode = false;
			}
			else if (mode) {
				query = query + " " + line;				
			}
		}
		br.close();
		return queries;
	}
	
}