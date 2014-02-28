// LauraRettig_TP1

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.apache.lucene.queryparser.classic.QueryParser;


public class Exercise1 {

	public static void main(String[] args) throws ParseException, IOException {
		
		// 1) Indexing
		
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
		Directory indexDirectory = new RAMDirectory();
		IndexWriter indexWriter = new IndexWriter(indexDirectory, new IndexWriterConfig(Version.LUCENE_47, analyzer)); 
		
		indexFiles(indexWriter, "docs");
		
		indexWriter.close();
		
		// 2) Searching
		
		try {
			searchQuery("city business program improvement income", 5, analyzer, indexDirectory);
			searchQuery("business improvement program city city income", 5, analyzer, indexDirectory);
		} catch (org.apache.lucene.queryparser.classic.ParseException e) {
			e.printStackTrace();
		}
	}
	
	private static void indexFiles(IndexWriter w, String location) throws IOException {
		// index all documents from the directory "location"
			File dir = new File(location);
	        File[] files = dir.listFiles();
	        for (File file : files) {
	            Document document = new Document();
	            String name = file.getName();
	            document.add(new StringField("name", name, Field.Store.YES));
	            Reader freader = new FileReader(file);
	            document.add(new TextField("content", freader));
	            w.addDocument(document);
	        }
	}
	
	private static void searchQuery(String q, int n, Analyzer a, Directory d) throws org.apache.lucene.queryparser.classic.ParseException, IOException {
		// search and print results
		Query query = new QueryParser(Version.LUCENE_47,"content",a).parse(q);
		IndexReader reader = DirectoryReader.open(d); 
		IndexSearcher searcher = new IndexSearcher(reader);
		
		TopScoreDocCollector res = TopScoreDocCollector.create(n, true);
		searcher.search(query, res);
		ScoreDoc[] hits = res.topDocs().scoreDocs;
		
		System.out.println("Top " + n + " documents, ranked for the query \"" + q + "\":");
		int i = 1;
		for (ScoreDoc hit : hits) {
			System.out.println(i + ". " + searcher.doc(hit.doc).getValues("name")[0] + " score: " + hit.score);
			i++;
		}
		System.out.println("");
		reader.close();
	}
	
}