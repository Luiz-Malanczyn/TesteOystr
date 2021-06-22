package Oystr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TryConnection {
	
	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);  // Create a Scanner object
	    System.out.println("Enter URL");

	    //Ajustando URL solicitada
	    String urlString = scanner.nextLine();  // Read user input
	    scanner.close();
		if(!urlString.contains("https://")) {
			urlString = "https://" + urlString;
		}
		if(!urlString.contains("www.")) {
			urlString = urlString.split("/")[0] + "//www." + urlString.split("https://")[1];
		}
		
		System.out.println("Analysing: " + urlString);
		
		//Realiza conexão com a URL
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		
		if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
			//Monta a String do código fonte
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
			    content.append(inputLine);
			}
			String SourceCode = content.toString().toLowerCase();
			
			//Verifica a versão do HTML de acordo com o código fonte
			if(SourceCode.contains("<!doctype html>")) {
				System.out.println("HTML Version: HTML 5");
			} else {
				String htlmVersion = regex(SourceCode, "dtd\\s+(\\w{4,5}\\s+\\d\\.\\d+)", 1);
				verificaRegex(htlmVersion, "Não foi possível determinar a versão do HTML da página.");
				System.out.println("HTML Version: " + htlmVersion);
			}
			
			//Verifica o título da página de acordo com o código fonte
			String titlePage = regex(SourceCode, "(<title([^>]+)>)([^>]+)<", 3);
			if(titlePage.isEmpty()) {
				titlePage = regex(SourceCode, "<title>([^>]+)<", 1);
				if(titlePage.isEmpty()) {
					verificaRegex(titlePage, "Não foi possível determinar o título da página.");
				}
			}
			System.out.println("Page title: " + titlePage);
			
			//Pegando links internos
			String siteName = regex(urlString, "www\\.([^.]+)", 1);
			List<List<String>> linksList = descobrirLinks(SourceCode, "href=\"https:\\/\\/([^>]+)\"", 1, siteName);
			System.out.println("Internal Links: " + linksList.get(0).size());
			System.out.println("External Links: " + linksList.get(1).size());
			
			in.close();
		} else {
			System.out.println("Conection error");
			System.exit(0);
		}
		con.disconnect();
	}
	
	//Método para utilizar o Regex
	public static String regex(String HTML, String patternStr, int group) {
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(HTML);
		if(matcher.find()){
			return matcher.group(group);
		}
		return "";
	}
	
	//Criando uma lista de listas para agrupar os tipos de Link
	public static List<List<String>> descobrirLinks(String HTML, String patternStr, int group, String siteName) {
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(HTML);
		List<String> internLinksLists = new ArrayList<String>();
		List<String> externLinksLists = new ArrayList<String>();
		while(matcher.find()){
			if(matcher.group(group).contains(siteName)) {
				internLinksLists.add(matcher.group(group));
			} else {
				externLinksLists.add(matcher.group(group));
			}
		}
		List<List<String>> listasLinks = new ArrayList<List<String>>();
		listasLinks.add(internLinksLists);
		listasLinks.add(externLinksLists);
		return listasLinks;
	}
	
	//Verifica se o Regex funcionou
	public static void verificaRegex(String str, String errorMessage) {
		if(str.isEmpty()) {
			System.out.println(errorMessage);
			System.exit(0);
		}
	}
}
