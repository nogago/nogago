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
package mobac.data.gpx.gpx11;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * Two lat/lon pairs defining the extent of an element.
 * 
 * 
 * <p>
 * Java class for boundsType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name=&quot;boundsType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;attribute name=&quot;minlat&quot; use=&quot;required&quot; type=&quot;{http://www.topografix.com/GPX/1/1}latitudeType&quot; /&gt;
 *       &lt;attribute name=&quot;minlon&quot; use=&quot;required&quot; type=&quot;{http://www.topografix.com/GPX/1/1}longitudeType&quot; /&gt;
 *       &lt;attribute name=&quot;maxlat&quot; use=&quot;required&quot; type=&quot;{http://www.topografix.com/GPX/1/1}latitudeType&quot; /&gt;
 *       &lt;attribute name=&quot;maxlon&quot; use=&quot;required&quot; type=&quot;{http://www.topografix.com/GPX/1/1}longitudeType&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "boundsType")
public class BoundsType {

	@XmlAttribute(required = true)
	protected BigDecimal minlat;
	@XmlAttribute(required = true)
	protected BigDecimal minlon;
	@XmlAttribute(required = true)
	protected BigDecimal maxlat;
	@XmlAttribute(required = true)
	protected BigDecimal maxlon;

	/**
	 * Gets the value of the minlat property.
	 * 
	 * @return possible object is {@link BigDecimal }
	 * 
	 */
	public BigDecimal getMinlat() {
		return minlat;
	}

	/**
	 * Sets the value of the minlat property.
	 * 
	 * @param value
	 *            allowed object is {@link BigDecimal }
	 * 
	 */
	public void setMinlat(BigDecimal value) {
		this.minlat = value;
	}

	/**
	 * Gets the value of the minlon property.
	 * 
	 * @return possible object is {@link BigDecimal }
	 * 
	 */
	public BigDecimal getMinlon() {
		return minlon;
	}

	/**
	 * Sets the value of the minlon property.
	 * 
	 * @param value
	 *            allowed object is {@link BigDecimal }
	 * 
	 */
	public void setMinlon(BigDecimal value) {
		this.minlon = value;
	}

	/**
	 * Gets the value of the maxlat property.
	 * 
	 * @return possible object is {@link BigDecimal }
	 * 
	 */
	public BigDecimal getMaxlat() {
		return maxlat;
	}

	/**
	 * Sets the value of the maxlat property.
	 * 
	 * @param value
	 *            allowed object is {@link BigDecimal }
	 * 
	 */
	public void setMaxlat(BigDecimal value) {
		this.maxlat = value;
	}

	/**
	 * Gets the value of the maxlon property.
	 * 
	 * @return possible object is {@link BigDecimal }
	 * 
	 */
	public BigDecimal getMaxlon() {
		return maxlon;
	}

	/**
	 * Sets the value of the maxlon property.
	 * 
	 * @param value
	 *            allowed object is {@link BigDecimal }
	 * 
	 */
	public void setMaxlon(BigDecimal value) {
		this.maxlon = value;
	}

}
