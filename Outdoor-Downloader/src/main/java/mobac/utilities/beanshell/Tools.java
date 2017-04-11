package mobac.utilities.beanshell;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.security.SecureRandom;

import mobac.mapsources.MapSourceTools;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.interfaces.MapSpace;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class Tools {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface MethodDescription {
		String value();
	}

	public static final SecureRandom RND = new SecureRandom();
	public static final MapSpace OSM_MERCATOR = MercatorPower2MapSpace.INSTANCE_256;

	@MethodDescription("Converts an horizontal tile number on a certain zoom level "
			+ "into the corespondent longitude")
	public static double xTileToLon(int x, int zoom) {
		return OSM_MERCATOR.cXToLon(x, zoom);
	}

	@MethodDescription("Converts an vertical tile number on a certain zoom level "
			+ "into the corespondent latitude")
	public static double yTileToLat(int y, int zoom) {
		return OSM_MERCATOR.cYToLat(y, zoom);
	}

	@MethodDescription("Returns a random value. Range [0..<b>max</b>]")
	public int getRandomInt(int max) {
		return RND.nextInt(max + 1);
	}

	@MethodDescription("Converts a tile numer on a certain zoom level into a quad tree coordinate")
	public static String encodeQuadTree(int zoom, int tilex, int tiley) {
		return MapSourceTools.encodeQuadTree(zoom, tilex, tiley);
	}

	@MethodDescription("Returns a byte array of length <b>length</b> filled with random data.")
	public byte[] getRandomByteArray(int length) {
		byte[] buf = new byte[length];
		RND.nextBytes(buf);
		return buf;
	}

	@MethodDescription("Encodes the <b>binaryData</b> byte array to a "
			+ "base64 String without line breaks")
	public static String encodeBase64(byte[] binaryData) {
		return new String(Base64.encodeBase64(binaryData));
	}

	@MethodDescription("Decodes an base64 encoded String to a byte array")
	public static byte[] decodeBase64(String base64String) {
		return Base64.decodeBase64(base64String);
	}

	@MethodDescription("Encodes the <b>binaryData</b> byte array to a hexadecimal String "
			+ "without line breaks, leading 0x and spaces")
	public static String encodeHex(byte[] binaryData) throws DecoderException {
		return Hex.encodeHexString(binaryData);
	}

	@MethodDescription("Decodes an hexadecimal encoded String to a byte array. The string have to "
			+ "contain only the hexadecimal encoded nibbles.")
	public static byte[] decodeHex(String hexString) throws DecoderException {
		return Hex.decodeHex(hexString.toCharArray());
	}

}
