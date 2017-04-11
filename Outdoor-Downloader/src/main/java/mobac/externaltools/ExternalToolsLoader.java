/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.externaltools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import mobac.program.DirectoryManager;
import mobac.utilities.GUIExceptionHandler;
import mobac.utilities.Utilities;
import mobac.utilities.file.FileExtFilter;

import org.w3c.dom.Document;

public class ExternalToolsLoader {

	public static List<ExternalToolDef> tools = null;

	static {
		load();
	}

	public static boolean load() {
		try {
			File dir = DirectoryManager.toolsDir;
			if (!dir.isDirectory())
				return false;
			File[] files = dir.listFiles(new FileExtFilter(".xml"));
			tools = new LinkedList<ExternalToolDef>();
			for (File f : files) {
				tools.add(loadFile(f));
			}
			return true;
		} catch (Exception e) {
			GUIExceptionHandler.showExceptionDialog("Failed to load external tools definition", e);
			return false;
		}
	}

	public static ExternalToolDef loadFile(File f) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(ExternalToolDef.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		InputStream is = null;
		try {
			is = new FileInputStream(f);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder loader = factory.newDocumentBuilder();
			Document document = loader.parse(is);
			return (ExternalToolDef) unmarshaller.unmarshal(document);
		} catch (JAXBException e) {
			throw e;
		} catch (Exception e) {
			throw new JAXBException(e);
		} finally {
			Utilities.closeStream(is);
		}
	}

	public static void save(ExternalToolDef toolsDef, File f) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(ExternalToolDef.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		OutputStream os = null;
		try {
			os = new FileOutputStream(f);
			marshaller.marshal(toolsDef, os);
		} catch (FileNotFoundException e) {
			throw new JAXBException(e);
		} finally {
			Utilities.closeStream(os);
		}
	}

	public static final void main(String[] args) {
		try {
			ExternalToolDef t = new ExternalToolDef();
			t.command = "command";
			t.name = "test";
			t.parameters.add(ToolParameters.MAX_LAT);
			t.parameters.add(ToolParameters.MAX_LON);
			t.parameters.add(ToolParameters.MIN_LAT);
			t.parameters.add(ToolParameters.MIN_LON);
			save(t, new File("tools/test.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
