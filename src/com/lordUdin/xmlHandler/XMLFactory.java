/**
 * 
 */
package com.lordUdin.xmlHandler;

import static com.lordUdin.xmlHandler.Configuration.DOWNLOAD_LIST_FILE;
import static com.lordUdin.xmlHandler.Configuration.SAVED_DOWNLOADS_FILE;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.lordUdin.downloader.Metadata;
import com.lordUdin.object.PartsMetadata;
import com.lordUdin.object.Status;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * @author lord udin
 *
 */
public class XMLFactory {
	private static XMLFactory instance = null; // Make the class singleton
	
	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;
	private TransformerFactory transFactory;
	private Transformer transformer;
	
	private XMLFactory() {
		
		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			
			transFactory = TransformerFactory.newInstance();
			transformer = transFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
//            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		} catch (Exception e) {
			factory = null;
			builder = null;
		}
	}
	
	public static XMLFactory newXMLFactory() {
		if(instance == null)
			instance = new XMLFactory();
		
		return instance;
	}
	

	public synchronized void createDownloadFileListXML() throws IOException {
		/*
		 * Structur xml
		 * ---------------------
		 * 	<downloadlist>
		 * 		<download id="...">
		 * 			<starttime>...</starttime>
		 * 			<endtime>...</endtime>
		 * 			<status>...</status>
		 * 			<completed>...</completed>
		 * 			<url>...</url>
		 * 			<file>
		 * 				<name>...</name>
		 * 				<type>...</type>
		 * 				<size>...</size>
		 * 				<path>...</path>
		 * 			</file>
		 * 		</download>
		 * 	</downloadlist>
		 */
		try {
			Document document = builder.newDocument();
			document.setXmlStandalone(false);
			document.setStrictErrorChecking(true);
			
			Element root = document.createElement("downloadlist");
			document.appendChild(root);
			
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new FileOutputStream(DOWNLOAD_LIST_FILE));
			
			transformer.transform(source, result);
			
		} catch (TransformerException e) {
			throw new IOException("[ERROR] Unable to create DownloadFileListXML file. " + e.getMessage());
		}
	}
	
	public synchronized List<Metadata> getDownloadList() throws IOException {
		List<Metadata> list = new ArrayList<>();
		
		try {
			Document document = builder.parse(DOWNLOAD_LIST_FILE);
			
			// Add root element to the XML
			Element root 	= document.getDocumentElement();
			NodeList downloadNodes	= root.getElementsByTagName("download");
			
			// Iterate through all downloadNodes
			for(int i=0; i<downloadNodes.getLength(); i++) {
				Element downloadElem = (Element) downloadNodes.item(i);
				

				long id 			= Long.parseLong(downloadElem.getAttribute("id"));
				boolean rangeAllowed= Boolean.parseBoolean(downloadElem.getAttribute("range"));
				
				Element startElem	= (Element) downloadElem.getElementsByTagName("starttime").item(0);
				String text 		= startElem.getTextContent();
				Date startTime		= null;
				if (text != null && !text.isEmpty()) {
					long sTime = Long.parseLong(text);
					startTime = new Date();
					startTime.setTime(sTime);
				}
				
				Element endElem 	= (Element) downloadElem.getElementsByTagName("endtime").item(0);
				text 				= endElem.getTextContent();
				Date endTime 		= null;
				if (text != null && !text.isEmpty()) {
					long eTime = Long.parseLong(text);
					endTime = new Date();
					endTime.setTime(eTime);
				}
				
				Element statusElem	= (Element) downloadElem.getElementsByTagName("status").item(0);
				String status		= statusElem.getTextContent();
				Status downStatus = Status.valueOf(status);
				
				Element compElem	= (Element) downloadElem.getElementsByTagName("completed").item(0);
				long completed		= Long.parseLong(compElem.getTextContent());
				
				Element urlElem		= (Element) downloadElem.getElementsByTagName("url").item(0);
				String url			= urlElem.getTextContent();
				
				Element fileElem	= (Element) downloadElem.getElementsByTagName("file").item(0);
				Element nameElem	= (Element) fileElem.getElementsByTagName("name").item(0);
				String fname		= nameElem.getTextContent();
				
				Element typeElem	= (Element) fileElem.getElementsByTagName("type").item(0);
				String ftype		= typeElem.getTextContent();
				
				Element sizeElem	= (Element) fileElem.getElementsByTagName("size").item(0);
				text 				= sizeElem.getTextContent();
				long fsize			= 0;
				if(text!= null && !text.isEmpty()) {
					fsize			= Long.parseLong(text);
				}
				
				Element fpathElem	= (Element) fileElem.getElementsByTagName("path").item(0);
				String fpath		= fpathElem.getTextContent();
				
				Metadata meta = new Metadata(id, url, startTime, endTime, fpath, downStatus, completed,
						fname, ftype, fsize, rangeAllowed);

				list.add(meta);
			}
			
		} catch (SAXException e) {
			throw new IOException("[ERROR] Unable to read DownloadFileListXML file. " + e.getMessage());
		}
		
		return list;
	}
	
	public synchronized Metadata getDownloadMetadata(long downloadId) throws IOException {
		try {
			Document document = builder.parse(DOWNLOAD_LIST_FILE);
			
			Element root 	= document.getDocumentElement();
			NodeList downloadNodes	= root.getElementsByTagName("download");
			
			for(int i=0; i<downloadNodes.getLength(); i++) {
				Element downloadElem = (Element) downloadNodes.item(i);

				long id	= Long.parseLong(downloadElem.getAttribute("id"));
				
				if(id != downloadId)
					continue;
				
				boolean rangeAllowed= Boolean.parseBoolean(downloadElem.getAttribute("range"));
				
				Element startElem	= (Element) downloadElem.getElementsByTagName("starttime").item(0);
				String text 		= startElem.getTextContent();
				Date startTime		= null;
				if (text != null && !text.isEmpty()) {
					long sTime = Long.parseLong(text);
					startTime = new Date();
					startTime.setTime(sTime);
				}
				
				Element endElem 	= (Element) downloadElem.getElementsByTagName("endtime").item(0);
				text 				= endElem.getTextContent();
				Date endTime 		= null;
				if (text != null && !text.isEmpty()) {
					long eTime = Long.parseLong(text);
					endTime = new Date();
					endTime.setTime(eTime);
				}
				
				Element statusElem	= (Element) downloadElem.getElementsByTagName("status").item(0);
				String status		= statusElem.getTextContent();
				Status downStatus = Status.valueOf(status);
				
				Element compElem	= (Element) downloadElem.getElementsByTagName("completed").item(0);
				long completed		= Long.parseLong(compElem.getTextContent());
				
				Element urlElem		= (Element) downloadElem.getElementsByTagName("url").item(0);
				String url			= urlElem.getTextContent();
				
				Element fileElem	= (Element) downloadElem.getElementsByTagName("file").item(0);
				Element nameElem	= (Element) fileElem.getElementsByTagName("name").item(0);
				String fname		= nameElem.getTextContent();
				
				Element typeElem	= (Element) fileElem.getElementsByTagName("type").item(0);
				String ftype		= typeElem.getTextContent();
				
				Element sizeElem	= (Element) fileElem.getElementsByTagName("size").item(0);
				text 				= sizeElem.getTextContent();
				long fsize			= 0;
				if(text!= null && !text.isEmpty()) {
					fsize			= Long.parseLong(text);
				}
				
				Element fpathElem	= (Element) fileElem.getElementsByTagName("path").item(0);
				String fpath		= fpathElem.getTextContent();
				
				Metadata meta = new Metadata(id, url, startTime, endTime, fpath, downStatus, completed,
						fname, ftype, fsize, rangeAllowed);
				
				return meta;
			}
			
		} catch (SAXException e) {
			throw new IOException("[ERROR] Unable to read DownloadFileListXML file. " + e.getMessage());
		}
		
		return null;
	}
	
	public synchronized void updateDownloadList(Metadata metadata) throws IOException {
		try {
			Document document = builder.parse(DOWNLOAD_LIST_FILE);
			
			// Add root element to the XML
			Element root 	= document.getDocumentElement();
			NodeList downloadNodes	= root.getElementsByTagName("download");
			
			Element downloadElem = null;
			// Iterate through all downloadNodes
			for(int i=0; i<downloadNodes.getLength(); i++) {
				Element elem = (Element) downloadNodes.item(i);
				
				String id 	= elem.getAttribute("id");
				String _id 	= metadata.getId() + ""; 
				
				// If download element is found
				if(id.equals(_id)) {
					downloadElem = elem;
					break;
				}
			}
			
			// If download element is found
			if(downloadElem != null) 
				root.removeChild(downloadElem);

			downloadElem = document.createElement("download");
			downloadElem.setAttribute("id", metadata.getId() + "");
			downloadElem.setAttribute("range", metadata.isRangeAllowed()+"");

			Element start = document.createElement("starttime");
			Date startTime = metadata.getStartTime();
			if(startTime != null)
			start.setTextContent(startTime.getTime() + "");

			Element end = document.createElement("endtime");
			Date endTime = metadata.getEndTime();
			if(endTime != null)
			end.setTextContent(endTime.getTime() + "");

			Element status = document.createElement("status");
			status.setTextContent(metadata.getStatus().name());
			
			Element completed = document.createElement("completed");
			completed.setTextContent(metadata.getCompleted() + "");

			Element url = document.createElement("url");
			url.setTextContent(metadata.getUrl());

			Element file = document.createElement("file");
			Element fname = document.createElement("name");
			fname.setTextContent(metadata.getFileName());

			Element ftype = document.createElement("type");
			ftype.setTextContent(metadata.getFileType());

			Element fsize = document.createElement("size");
			fsize.setTextContent(metadata.getFileSize() + "");

			Element fpath = document.createElement("path");
			fpath.setTextContent(metadata.getFilePath());

			file.appendChild(fname);
			file.appendChild(ftype);
			file.appendChild(fsize);
			file.appendChild(fpath);
			
			downloadElem.appendChild(start);
			downloadElem.appendChild(end);
			downloadElem.appendChild(status);
			downloadElem.appendChild(completed);
			downloadElem.appendChild(url);
			downloadElem.appendChild(file);
			
			root.appendChild(downloadElem);

			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new FileOutputStream(DOWNLOAD_LIST_FILE));
			
			// Save the result
			transformer.transform(source, result);
			
		} catch (SAXException | TransformerException e) {
			throw new IOException("[ERROR] Unable to create DownloadFileListXML file. " + e.getMessage());
		}
	}
	
	public synchronized void removeDownloadMetadata(long downloadId) throws IOException {
		try {
			Document document = builder.parse(DOWNLOAD_LIST_FILE);
			
			Element root 	= document.getDocumentElement();
			NodeList downloadNodes	= root.getElementsByTagName("download");
			
			Element downloadElem = null;
			for(int i=0; i<downloadNodes.getLength(); i++) {
				Element elem = (Element) downloadNodes.item(i);
				
				String id 	= elem.getAttribute("id");
				String _id 	= downloadId + ""; 
				
				if(id.equals(_id)) {
					downloadElem = elem;
					break;
				}
			}
			
			if (downloadElem != null) {
				root.removeChild(downloadElem);

				DOMSource source = new DOMSource(document);
				StreamResult result = new StreamResult(new FileOutputStream(DOWNLOAD_LIST_FILE));

				transformer.transform(source, result);
			}
			
		} catch (SAXException | TransformerException e) {
			throw new IOException("[ERROR] Unable to create DownloadFileListXML file. " + e.getMessage());
		}
	}
	
	public synchronized void clearDownloadList() throws IOException {
		createDownloadFileListXML();
		createSavedDownloadPartsXML();
	}
	

	public synchronized void createSavedDownloadPartsXML() throws IOException {
		/*
		 * XML structure
		 * ---------------------
		 * 	<filelist>
		 * 		<download id="..." completed="...">
		 * 			<part id="...">
		 * 				<start>...</start>
		 * 				<end>...</end>
		 * 				<path>...</path>
		 * 			</part>
		 * 		</download>
		 * 	</filelist>
		 */
		try {
			Document document = builder.newDocument();
			document.setXmlStandalone(false);
			document.setStrictErrorChecking(true);
			
			// Add root element to the XML
			Element root = document.createElement("filelist");
			document.appendChild(root);
			
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new FileOutputStream(SAVED_DOWNLOADS_FILE));
			
			// Save the result
			transformer.transform(source, result);
			
		} catch (TransformerException e) {
			throw new IOException("[ERROR] Unable to create Saved Download File. " + e.getMessage());
		}
	}


	public synchronized List<PartsMetadata> getDownloadPartsList(long downloadId) throws IOException {
		List<PartsMetadata> list = new ArrayList<>();

		try {
			Document document = builder.parse(SAVED_DOWNLOADS_FILE);
			
			Element root = document.getDocumentElement();
			
			NodeList downloadListElem = root.getElementsByTagName("download");
			
			Element downloadElem = null;
			
			for(int i=0; i<downloadListElem.getLength(); i++) {
				Element elem = (Element) downloadListElem.item(i);
				
				long id = Long.parseLong(elem.getAttribute("id"));
				
				if(id == downloadId) {
					downloadElem = elem;
					break;
				}
			}
			
			NodeList downloadParts = downloadElem.getElementsByTagName("part");

			for (int i = 0; i < downloadParts.getLength(); i++) {
				Element partElem = (Element) downloadParts.item(i);

				Element startElem = (Element) partElem.getElementsByTagName("start").item(0);
				long start = Long.parseLong(startElem.getTextContent());

				Element endElem = (Element) partElem.getElementsByTagName("end").item(0);
				long end = Long.parseLong(endElem.getTextContent());

				Element pathElem = (Element) partElem.getElementsByTagName("path").item(0);
				String path = pathElem.getTextContent();
				
				int id = Integer.parseInt(partElem.getAttribute("id"));
				
				PartsMetadata parts = new PartsMetadata(downloadId, id, start, end, path);
				
				list.add(parts);
			}
		} catch (SAXException e) {
			throw new IOException("[ERROR] Unable to update Download Part File. " + e.getMessage());
		}
		
		return list;
	}
	
	

	public synchronized void updateSavedDownloadParts(PartsMetadata metadata) throws IOException {
		try {
			Document document = builder.parse(SAVED_DOWNLOADS_FILE);
			
			Element root = document.getDocumentElement();
			
			NodeList downloadList = root.getElementsByTagName("download");
			
			Element downloadElem = null;
			for(int i=0; i<downloadList.getLength(); i++) {
				Element elem = (Element) downloadList.item(i);
				
				long downId = Long.parseLong(elem.getAttribute("id"));
				
				if(downId == metadata.getDownloadId()) {
					downloadElem = elem;
					break;
				}
 			}
			
			if (downloadElem == null) {
				downloadElem = document.createElement("download");
				downloadElem.setAttribute("id", metadata.getDownloadId() + "");
				
				root.appendChild(downloadElem);
			}			
			
			NodeList partList = downloadElem.getElementsByTagName("part");
			
			Element downPartElem = null;
			
			for(int i=0; i<partList.getLength(); i++) {
				Element elem = (Element) partList.item(i);
				long id = Long.parseLong(elem.getAttribute("id"));
				
				if(id == metadata.getId()) {
					downPartElem = elem;
					break;
				}
			}
			
			if(downPartElem == null) {
				downPartElem = document.createElement("part");
				downPartElem.setAttribute("id", metadata.getId() + "");
				
				Element startElem 	= document.createElement("start");
				startElem.appendChild(document.createTextNode(metadata.getStart() + ""));
				
				Element endElem 	= document.createElement("end");
				endElem.appendChild(document.createTextNode(metadata.getEnd() + ""));
				
				Element pathElem	= document.createElement("path");
				pathElem.appendChild(document.createTextNode(metadata.getPath() + ""));

				downPartElem.appendChild(startElem);
				downPartElem.appendChild(endElem);
				downPartElem.appendChild(pathElem);
				
				downloadElem.appendChild(downPartElem);
			} else {
				Element startElem 	= (Element) downPartElem.getElementsByTagName("start").item(0);
				if(startElem.hasChildNodes())
					startElem.removeChild(startElem.getFirstChild());
				
				startElem.appendChild(document.createTextNode(metadata.getStart() + ""));
			}
			
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new FileOutputStream(SAVED_DOWNLOADS_FILE));
			
			transformer.transform(source, result);
			
		} catch (SAXException | TransformerException e) {
			throw new IOException("[ERROR] Unable to update Download Part File. " + e.getMessage());
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	

	public synchronized void removeSavedDownloadParts(PartsMetadata metadata) throws IOException {
		try {
			Document document = builder.parse(SAVED_DOWNLOADS_FILE);
			
			Element root = document.getDocumentElement();
			
			NodeList downloadList = root.getElementsByTagName("download");
			
			Element downloadElem = null;
			for(int i=0; i<downloadList.getLength(); i++) {
				Element elem = (Element) downloadList.item(i);
				
				long downId = Long.parseLong(elem.getAttribute("id"));
				
				if(downId == metadata.getDownloadId()) {
					downloadElem = elem;
					break;
				}
 			}
			
			if (downloadElem == null) return;
			
			
			NodeList partList = downloadElem.getElementsByTagName("part");
			
			Element downPartElem = null;
			
			for(int i=0; i<partList.getLength(); i++) {
				Element elem = (Element) partList.item(i);
				long id = Long.parseLong(elem.getAttribute("id"));
				
				if(id == metadata.getId()) {
					downPartElem = elem;
					break;
				}
			}
			
			if(downPartElem == null) return;
			
			downloadElem.removeChild(downPartElem);
			boolean hasElementNode = false;
			
			NodeList nodes = downloadElem.getChildNodes();
			for(int i=0; i<nodes.getLength(); i++) {
				Node elem = nodes.item(i);
				
				if(elem.getNodeType() == Node.ELEMENT_NODE) {
					hasElementNode = true;
					break;
				}
			}
			
			if(!hasElementNode)
				root.removeChild(downloadElem);
			
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new FileOutputStream(SAVED_DOWNLOADS_FILE));
			
			transformer.transform(source, result);
			
		} catch (SAXException | TransformerException e) {
			throw new IOException("[ERROR] Unable to remove Download Part File. " + e.getMessage());
		}
	}
}
