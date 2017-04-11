@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters({
	@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(
		type = mobac.program.model.TileImageType.class, 
		value = mobac.program.jaxb.TileImageTypeAdapter.class
	),
	@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(
			type = java.awt.Color.class, 
			value = mobac.program.jaxb.ColorAdapter.class
		)
	
})
package mobac.mapsources.custom;