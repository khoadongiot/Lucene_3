package khoa;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
public class Bai3 {

	public static final String FILES_TO_INDEX_DIRECTORY = "Data";
	public static final String INDEX_DIRECTORY = "indexDirectory";

	public static final String FIELD_PATH = "path";
	public static final String FIELD_CONTENTS = "contents";
	
	

	public static void createIndex() throws CorruptIndexException, LockObtainFailedException, IOException {
		Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexWriter indexWriter = new IndexWriter(directory, config);
		File dir = new File(FILES_TO_INDEX_DIRECTORY);
		File[] files = dir.listFiles();
		for (File file : files) {
			
			Document document = new Document();

			String path = file.getCanonicalPath();
			document.add(new TextField(FIELD_PATH, path, Field.Store.YES ));

			Reader reader = new FileReader(file);
			document.add(new TextField(FIELD_CONTENTS, reader));

			indexWriter.addDocument(document);
		}
		indexWriter.close();
	}

	public static void searchIndex(String searchString) throws IOException, ParseException {
		
		int hitsPerPage = 10;
		Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
		
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser queryParser = new QueryParser(FIELD_CONTENTS, analyzer);
		Query query =  queryParser.parse(searchString);
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		TopDocs docs = indexSearcher.search(query, hitsPerPage);
		ScoreDoc[] hits = docs.scoreDocs;
		
		System.out.println("Searching for '" + searchString + "'");
		System.out.println("Found " + hits.length + " hits.");
		
		 for(int i=0;i<hits.length;++i) {
	            int docId = hits[i].doc;
	            Document d = indexSearcher.doc(docId);
	            String path = d.get(FIELD_PATH);
	            System.out.println((i + 1) + ". " + path + "\t" + hits[i].score);
	     }	
	}
	
	
	private static void delete(File file) throws IOException {
        if (file.isDirectory()) {
           if (file.list().length == 0) {
               file.delete();
           } else {
                String files[] = file.list();
                for (String temp : files) {
                   File fileDelete = new File(file, temp);
                   delete(fileDelete);
                }
                if (file.list().length == 0) {
                    file.delete();
                }
            }
        } else {
            file.delete();
        }
     }
	
	public static void main(String[] args) throws Exception {
		File directory = new File(INDEX_DIRECTORY);
		System.out.print("Enter the word you want to search: ");
		String s = new Scanner(System.in).nextLine(); 
        if (!directory.exists()) {
        	createIndex();
    		searchIndex(s);
        } else {
                 delete(directory);
                 createIndex();
         		 searchIndex(s);
      
        }
	}
}