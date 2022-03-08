package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.json.JSONArray;
import org.json.JSONObject;


public class NationalLibraryOfKorea {
	
	public void checkData(String uri) {
		try {
//			1. 발행된 데이터의 URI로 접근하여 데이터 가져오기 & 내용 확인
			URL url = new URL(uri);
			URLConnection conn = url.openConnection();

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line;
			while ((line = br.readLine()) != null)
				System.out.println(line);			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getDataWithJenaStatement(String uri) {
//		1. Jena Model에 데이터 담기
		Model model = FileManager.get().loadModel(uri);
//		model.write(System.out, "TTL");		// Model에 담긴 내용을 화면에 출력

//		2. Jena Model에 담긴 데이터를 S-P-O 단위인 Statement의 리스트로 변경
		StmtIterator iter = model.listStatements(ResourceFactory.createResource(uri), null, (RDFNode) null);

		Statement stmt = null;
		String sub = null, prop = null, obj = null;

		while (iter.hasNext()) {
			stmt = iter.next();

			// Subject
			sub = stmt.getSubject().toString();
			// Predicate
			prop = stmt.getPredicate().toString();
			// Object
			obj = stmt.getObject().toString();

			// Predicate가 birthYear일 경우 화면에 출력
			if (prop.equals("http://lod.nl.go.kr/ontology/birthYear")) {
//				System.out.println(sub + "\t" + prop + "\t" + obj);
				// Object의 값만 출력
				System.out.println("Result: " + stmt.getObject().asNode().getLiteralValue());
			}
		}
	}

	public void getDataWithJenaSPARQL(String uri) {
//		1. SPARQL 질의문 생성 
//		String queryString = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT ?name WHERE {?s foaf:name ?name}" ;
		String queryString = "PREFIX nlon: <http://lod.nl.go.kr/ontology/> PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT ?birthYear WHERE {?s nlon:birthYear ?birthYear}";
		Query query = QueryFactory.create(queryString);
//			query.serialize(new IndentedWriter(System.out, true)) ;

//		2. SPARQL 질의문으로 Jena Model에 데이터 담기
		Model model = FileManager.get().loadModel(uri);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		// SELECT 질의를 실행하여 결과값 받아오기
		ResultSet rs = qexec.execSelect();

		for (; rs.hasNext();) {
			QuerySolution rb = rs.nextSolution();

			// SELECT 하는 변수
			RDFNode x = rb.get("birthYear");

			// 결과값 체크
			if (x.isLiteral()) {
				Literal titleStr = (Literal) x;
				String result = titleStr.getString().substring(0, 4);
				System.out.println("Result: " + result);
			} else
				System.out.println("Strange - not a literal: " + x);
		}
	}
	
	public void getDataWithSPARQLEndpoint() {
//		1. SPARQL 질의문 생성 
//		String queryString = "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT * WHERE"
//				+ " { SERVICE <http://lod.nl.go.kr/sparql> {?s a owl:ObjectProperty . } }" ; 
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + 
				"PREFIX nlon: <http://lod.nl.go.kr/ontology/> PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
				 "SELECT ?id ?name ?birthYear " + 
				"WHERE { " +
				 "SERVICE <http://lod.nl.go.kr/sparql> {" + 
				"?id rdf:type nlon:Author ; " + 
				"	foaf:name '아이유' . " + 
				"?id foaf:name ?name . " + 
				"?id nlon:birthYear ?birthYear  ." + 
				"} }";
		
//		2. SPARQL 질의문으로 DefaultModel에 데이터 담기
		Query query = QueryFactory.create(queryString) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, ModelFactory.createDefaultModel()); 

    	ResultSet rs = qexec.execSelect() ;
    	ResultSetFormatter.out(System.out, rs, query) ;
    	
//    	json으로 결과 출력
//        ByteArrayOutputStream b = new ByteArrayOutputStream();
//    	ResultSetFormatter.outputAsJSON(b, rs);
//    	System.out.println(b);
	}

	public static void main(String[] args) {
		String uri = "http://lod.nl.go.kr/resource/KAC201514275";

		NationalLibraryOfKorea ex = new NationalLibraryOfKorea();
//		ex.checkData(uri);
		ex.getDataWithJenaStatement(uri);
		ex.getDataWithJenaSPARQL(uri);
		ex.getDataWithSPARQLEndpoint();
	}

}
