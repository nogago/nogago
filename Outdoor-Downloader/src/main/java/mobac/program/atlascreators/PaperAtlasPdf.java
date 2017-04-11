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
package mobac.program.atlascreators;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import mobac.exceptions.MapCreationException;
import mobac.program.ProgramInfo;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.model.UnitSystem;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

@AtlasCreatorName(value = "Paper Atlas (PDF)")
public class PaperAtlasPdf extends PaperAtlas {

	private Document document;

	public PaperAtlasPdf() {
		super(false);
	}

	private Document createDocument(Rectangle r) throws MapCreationException {
		File pdfFile = new File(getLayerFolder(), map.getName() + ".pdf");
		float left = (float) s.marginLeft;
		float right = (float) s.marginRight;
		float top = (float) s.marginTop;
		float bottom = (float) s.marginBottom;
		Document document = new Document(r, left, right, top, bottom);
		PdfWriter pdfWriter;
		try {
			pdfFile.createNewFile();
			pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		} catch (DocumentException e) {
			throw new MapCreationException(map, e);
		}
		pdfWriter.setCompressionLevel(s.compression);
		document.open();
		document.addAuthor(ProgramInfo.PROG_NAME);
		document.addCreationDate();
		document.addCreator(ProgramInfo.PROG_NAME);
		document.addProducer();
		return document;
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {

		if (s.paperSize != null) {
			document = createDocument(s.paperSize.createRectangle());
		}

		try {
			super.createMap();
		} finally {
			try {
				document.close();
			} catch (Exception e) {
				new MapCreationException(map, e);
			}
			document = null;
		}
	}

	@Override
	protected void processPage(BufferedImage image, int pageNumber) throws MapCreationException {
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		if (document == null) {
			double width = UnitSystem.pixelsToPoints(imageWidth, s.dpi);
			double height = UnitSystem.pixelsToPoints(imageHeight, s.dpi);
			width += s.marginLeft + s.marginRight;
			height += s.marginTop + s.marginBottom;
			Rectangle r = new Rectangle((float) width, (float) height);
			document = createDocument(r);
		}

		Image iTextImage;
		try {
			iTextImage = Image.getInstance(image, Color.WHITE);
		} catch (BadElementException e) {
			throw new MapCreationException(map, e);
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		}
		iTextImage.setCompressionLevel(s.compression);
		iTextImage.setDpi(s.dpi, s.dpi);

		float width = (float) UnitSystem.pixelsToPoints(imageWidth, s.dpi);
		float height = (float) UnitSystem.pixelsToPoints(imageHeight, s.dpi);
		iTextImage.scaleAbsolute(width, height);

		try {
			document.add(iTextImage);
		} catch (DocumentException e) {
			throw new MapCreationException(map, e);
		}
		document.newPage();
	}

}
