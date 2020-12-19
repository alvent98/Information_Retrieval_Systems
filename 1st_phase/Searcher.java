import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class Searcher {
	private Searcher() {};
	
	public static void search(IndexSearcher euSearcher, Query q, int qId, int hitsInPage) throws IOException {
		TopDocs euResults = euSearcher.search(q, hitsInPage);
		ScoreDoc[] hits = euResults.scoreDocs;
		boolean toPrintInConsole = true;
		if(toPrintInConsole) {
			int numTotalHits = Math.toIntExact(euResults.totalHits);
		    System.out.println(numTotalHits + " total matching documents");
		    System.out.println("q_id "+"iter  "+"docno    "+"rank    "+"sim     "+"run_id");
		} 
	    //These are for file output. To be customized in case it runs in other computer
	    Path file = Paths.get("C:\\Users\\a\\Desktop\\txts\\results.txt");
	    List<String> lines = new ArrayList<String>();
	    String formattedScore;
	    for(ScoreDoc sd : hits) {
	    	Document currentDocument = euSearcher.doc(sd.doc);
	    	if(toPrintInConsole) {
		    	System.out.print(qId+"     "+"0    "+currentDocument.get("id")+"    0    ");
		    	System.out.printf("%8.5f", sd.score);
		    	System.out.println("   search");
	    	}
	    	formattedScore = String.format("%8.5f", sd.score);
	    	String queryIdPrefix = (qId<10)? "Q0" : "Q";
	    	lines.add(queryIdPrefix+qId+"     "+"Q0    "+currentDocument.get("id")+"    0    "+formattedScore+"   search");
	    }
	    Files.write(file, lines, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
	}
	
	//Returns a List<String> with all the queries in the relative txt file
	private static List<String> getQueries(String queriesPath) {
		Path queriesFile = Paths.get(queriesPath);
		InputStream stream = null;
		List<String> queries = new ArrayList<String>();
		String line = "";
		try {
			stream = Files.newInputStream(queriesFile);
			BufferedReader queriesReader = new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8));
			
			while ((line = queriesReader.readLine()) != null) {
				line = line.trim();
				if(hasOnlyCharsAndSpaces(line)) {
					queries.add(line);
				}
			}
			
		} catch (IOException e) {
			System.err.println("Caught "+e.getClass()+", "+e.getMessage());
		}
		
		return queries;	
	}
	
	//Returns true if the parameter contains only letters
	public static boolean hasOnlyCharsAndSpaces(String name) {
	    return name.matches("[a-zA-Z- ]+");
	}
	
	public static void main(String[] args) throws Exception {
		String index = "index";
		String field = "contents";
		
		//To be customized in case it runs in other computer
		File resultsFile = new File("C:\\Users\\a\\Desktop\\txts\\results.txt");
		if (resultsFile.createNewFile()) {
	        System.out.println("File created: " + resultsFile.getName());
	    } else {
	        System.out.println("File already exists.");
	        //This line deletes old contents
	        new PrintWriter(resultsFile);
	    }
		//To be customized in case it runs in other computer
		List<String> queries = getQueries("C:\\Users\\a\\Desktop\\txts\\queries.txt");
		
		int hitsInPage = 20; //First is 20, then 30 then 50
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new EnglishAnalyzer();
		Query query = null;
		QueryParser parser = null;
		int id = 0;
		for(String q : queries) {
			id++;
			parser = new QueryParser(field, analyzer);
			query = parser.parse(q);
			System.out.println("Searching for: " + query.toString(field));
			search(searcher,query, id, hitsInPage);
		}
	}
}
