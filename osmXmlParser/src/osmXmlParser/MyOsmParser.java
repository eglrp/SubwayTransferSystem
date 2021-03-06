package osmXmlParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

class Station{
	String id;
	String name;
	String enName;
	String latitude;
	String longitude;
	
	Station (String i, String n, String e, String la, String lo){
		id=i;
		name=n;
		enName=e;
		latitude=la;
		longitude=lo;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Station) {
			Station t = (Station) obj;
			return this.name.equals(t.name);
		}
		return super.equals(obj);
	 }
}

class Route{
	String id;
	String name;
	String colour;
	String from;
	String to;
	int totalStation;
	
	List<Station> stationList;
	
	Route(){
		stationList=new ArrayList<Station>();
	}
}

public class MyOsmParser {

	HashMap<String,Station> hashMap;
	List<Route> routeList;
	
	MyOsmParser(){
		hashMap=new HashMap<String,Station>();
		routeList=new ArrayList<Route>();
	}
	
	public void parseStation(){
		String xmlPath="D:\\station.osm";
		try{
			SAXBuilder saxBuilder = new SAXBuilder(false);
			InputStream inputStream = new FileInputStream(new File(xmlPath));
			InputSource inputSource = new InputSource(inputStream);
			
			Document document = saxBuilder.build(inputSource);
			Element rootElement = document.getRootElement();
			
			File writeStationFile = new File("D:\\station.txt");
			writeStationFile.createNewFile();
			BufferedWriter out = new BufferedWriter (new FileWriter(writeStationFile));
			
			List<Element> stationList = rootElement.getChildren("node");
			for(Element e : stationList){
				String id="", name="", enName="", latitude="", longitude="";
				
				id = e.getAttributeValue("id");
				latitude = e.getAttributeValue("lat");
				longitude = e.getAttributeValue("lon");
				
				List<Element> tagList = e.getChildren("tag");
				for(Element tage: tagList){
					if(tage.getAttributeValue("k").equals("name"))
						name = tage.getAttributeValue("v");
					else if(tage.getAttributeValue("k").equals("name:en"))
						enName = tage.getAttributeValue("v");
				}
				
//				System.out.println(id+ "\t" + name + "\t" + latitude + "\t" + longitude);
				out.write(id+ "\t" + name + "\t" + latitude + "\t" + longitude + "\n");
				hashMap.put(id, new Station(id,name,enName,latitude,longitude));
			}
			out.flush();
			out.close();
		}catch(JDOMException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void parseRoute(){
		String xmlPath="D:\\route.osm";
		try{
			SAXBuilder saxBuilder = new SAXBuilder(false);
			InputStream inputStream = new FileInputStream(new File(xmlPath));
			InputSource inputSource = new InputSource(inputStream);
			
			Document document = saxBuilder.build(inputSource);
			Element rootElement = document.getRootElement();
			
			File writeStationFile = new File("D:\\route.txt");
			writeStationFile.createNewFile();
			BufferedWriter out = new BufferedWriter (new FileWriter(writeStationFile));
			
			List<Element> relationList = rootElement.getChildren("relation");
			for(Element e : relationList){
				Route route=new Route();
				
				List<Element> tagList =e.getChildren("tag");
				for(Element tage:tagList){
					String tagKey=tage.getAttributeValue("k");
					String tagValue=tage.getAttributeValue("v");
					
					if(tagKey.equals("ref")){
						if(tagValue.startsWith("M"))
							tagValue=tagValue.substring(1);
						route.id=tagValue;
					}
					else if(tagKey.equals("name"))
						route.name=tagValue;
					else if(tagKey.equals("colour"))
						route.colour=tagValue;
					else if(tagKey.equals("from"))
						route.from=tagValue;
					else if(tagKey.equals("to"))
						route.to=tagValue;
				}
				if(route.id==null)
					route.id="17";
				
				List<Element> memberList = e.getChildren("member");
				for(Element membere:memberList){
					String ref=membere.getAttributeValue("ref");
					if(hashMap.containsKey(ref)&&
							!route.stationList.contains(hashMap.get(ref)))
//							!route.stationList.get(route.stationList.size()-1).name.equals(hashMap.get(ref).name)))
						route.stationList.add(hashMap.get(ref));
				}
				route.totalStation=route.stationList.size();
				routeList.add(route);
			}
			
			Collections.sort(routeList,new Comparator<Route>(){
				public int compare(Route r1,Route r2){
					return Integer.valueOf(r1.id).compareTo(Integer.valueOf(r2.id));
				}
			});
			
			for(Route re:routeList){
				out.write("id: "+re.id+"\n");
				out.write("name: "+re.name+"\n");
				out.write("colour: "+re.colour+"\n");
				out.write("from: "+re.from+"\n");
				out.write("to: "+re.to+"\n");
				out.write("totalStation: "+re.totalStation+"\n");
				
				for(Station se:re.stationList)
					out.write(se.id+ "\t" + se.name + "\t" + se.latitude + "\t" + se.longitude + "\n");
				out.write("\n");
			}
			out.flush();
			out.close();
		}catch(JDOMException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}	
	}
		
	public static void main(String[] args) {
		MyOsmParser myOsmParser=new MyOsmParser();
		myOsmParser.parseStation();
		myOsmParser.parseRoute();
		
		System.out.println("xml�������");
	}

}
