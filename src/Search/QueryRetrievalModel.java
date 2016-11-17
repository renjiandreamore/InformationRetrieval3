package Search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import Classes.Query;
import Classes.Document;
import IndexingLucene.MyIndexReader;

public class QueryRetrievalModel {
	
	
	public double miu = 2000;   
	public long collectionLength = 142065539;
	protected MyIndexReader indexReader;
	
	
	public QueryRetrievalModel(MyIndexReader ixreader) {
		indexReader = ixreader;
	}
	
	/**
	 * Search for the topic information. 
	 * The returned results (retrieved documents) should be ranked by the score (from the most relevant to the least).
	 * TopN specifies the maximum number of results to be returned.
	 * 
	 * @param aQuery The query to be searched for.
	 * @param TopN The maximum number of returned document
	 * @return
	 */
	
	public List<Document> retrieveQuery( Query aQuery, int TopN ) throws IOException {
		// NT: you will find our IndexingLucene.Myindexreader provides method: docLength()
		// implement your retrieval model here, and for each input query, return the topN retrieved documents
		// sort the docs based on their relevance score, from high to low
		List<String> oneQuery = aQuery.GetQueryContent();
		HashMap<String, HashMap<Integer, Integer> > postings = new HashMap<>();
		HashSet<Integer> docSet = new HashSet<>();
		
		
		//for(String token : oneQuery){   //is not usable 
		for(int j = 0; j < oneQuery.size(); j++){
			String token = oneQuery.get(j);
			HashMap<Integer, Integer> map = new HashMap<>();
			int[][] posting = indexReader.getPostingList(token);
			if (posting == null || posting.length == 0) {
				oneQuery.remove(token);
				continue;
			}
			for(int i = 0; i < posting.length; i++){
					int id = posting[i][0];
					int freq = posting[i][1];
					map.put(id, freq);
					docSet.add(id);
					postings.put(token, map);
			}
			
		}
		//< hong-<0001-3, 0012-2, 0001-6,...>, econ-<0004-2, 0023-19, 0002-5, ...>, kong-<...>, singapo-<...> >
		
		
		List<Document> docResult = new ArrayList<>();
		
		Iterator<Integer> it = docSet.iterator();
		while(it.hasNext()){  //所有相关的文件里面
			int id = it.next(); 
			double score = 1;
			
			for(String token : oneQuery){
				long cf = indexReader.CollectionFreq(token);
				HashMap<Integer, Integer> map = postings.get(token);
				int docfrq = 0;
				if(map.containsKey(id)){
					docfrq = map.get(id);
				}
				
				double Pw = (docfrq + miu * cf / collectionLength) / (miu + indexReader.docLength(id));
				score = score * Pw;
			}
			
			if(score != 0){
				String docNo = indexReader.getDocno(id);
				String docId = String.valueOf(id);
				Document doc = new Document(docId, docNo, score);
				docResult.add(doc);
			}
		}
		
		Collections.sort(docResult, new Comparator<Document>(){
			public int compare(Document a, Document b){
				if(a.score() != b.score())
					return a.score()<b.score()? 1:-1;
				else 
					return 1;
			}
		});
		
		return docResult.subList(0, TopN);
		
		//return null;
	}
	
}