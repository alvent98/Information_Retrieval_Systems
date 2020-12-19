import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexCreator {

	//This function Indexes one document 
	static void indexDocument(final IndexWriter writer, Path file) throws IOException {
		try (InputStream stream = Files.newInputStream(file)){
			//Add the path in the document as a Field
			Field pathField = new StringField("path", file.toString(), Field.Store.YES);
			
			BufferedReader euReader = new BufferedReader(new InputStreamReader(stream, 
					StandardCharsets.UTF_8));
			
			String text = "";
			String line;
		   	Document euDocument = new Document();
		   	TextField euContents;
		   	StringField euId;
		   	String id;
		    id = euReader.readLine();
		    euId = new StringField("id", id.trim(), Field.Store.YES);
		    euDocument.add(euId);
		   	//This while-loop will add the new documents
			while ((line = euReader.readLine()) != null) {
				
				//If we found the delimiter between documents:
				if(line.contains("///")) {
					euContents = new TextField("contents", text, Store.YES);
				   	euDocument.add(pathField);
				   	euDocument.add(euContents);
				   	
				   	//Possibly the else branch is not necessary
					//Check whether the document already exists, in order not to overwrite
					//the old one in case it exists
					if(writer.getConfig().getOpenMode() == OpenMode.CREATE) {
						//Add the document
						writer.addDocument(euDocument);
						System.out.println("Document added");
					} else {	
						//Update the already existent document
						writer.updateDocument(new Term("path", file.toString()), euDocument);
						System.out.println("Document updated");
					}
					
					String[] euIds = euDocument.getValues("id");
			        for (String element: euIds) {
			            System.out.println("Id: "+element);
			        }
			        
					String[] euValues = euDocument.getValues("contents");
			        for (String element: euValues) {
			            System.out.println("Contents: "+element);
			        }  
			        
			        //Then proceed into reading the next document, and adding its ID
				   	text = "";
				   	euDocument = new Document();
				    id = euReader.readLine();
				    euId = new StringField("id", id.trim(), Field.Store.YES);
				    euDocument.add(euId);
			   	} else {
			   		text += line;
			   	}
			}
		}	  
	}

	public static void main(String args[]) {
		String indexPath = "index"; //Here will be added the path of the index
		String docsPath = null; //Here will be added the path to the document with the data
		
		//Path for the IR2020, to be customized in case it runs in other computer
		docsPath = "C:\\Users\\a\\Desktop\\txts\\documents.txt";
				
		//Check if there is an Improper or nonexistent document directory
		Path docDir = Paths.get(docsPath);
		if(!Files.isReadable(docDir)) {
			System.err.println("Improper or nonexistent document directory entered.");
		}
		
		try {
			//Initialize all special things Lucene does for us
			Directory directory = FSDirectory.open(Paths.get(indexPath));
			Analyzer analyzer = new EnglishAnalyzer();
			IndexWriterConfig indexWriterConf = new IndexWriterConfig(analyzer);
			
			indexWriterConf.setOpenMode(OpenMode.CREATE);
			
			//Create the index writer, in the directory and with the 
			//configuration previously specified
			IndexWriter writer = new IndexWriter(directory, indexWriterConf);
			
			//run the upper function
			indexDocument(writer, docDir);
			
			//close the writer
			writer.close();
			
		} catch (IOException e) {
			System.err.println("IOException caught!"+e.getClass()+" "+e.getMessage());
		}
	}
}