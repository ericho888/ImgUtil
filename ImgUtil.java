package pdfBox;

import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Base64;
import java.util.Iterator;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.sun.media.jai.codec.TIFFEncodeParam;
import com.sun.media.jai.codecimpl.TIFFImageEncoder;

public class ImgUtil {
	
	// #####################################################################
	// ##																  ##
	// ##								Read file						  ##
	// ##																  ##
	// #####################################################################
	
	public static byte[] readPdf(String path) {
		try {
			return Files.readAllBytes(Paths.get(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String readPdfEncode(String path) {
		byte[] decodedBytes;
		decodedBytes = readPdf(path);

		return encodeImg(decodedBytes);
	}
	
	public static BufferedImage readImg(String path) {;
		try {
			BufferedImage image = ImageIO.read(new File(path));
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// #####################################################################
	// ##																  ##
	// ##					PdfBox (PDF to TIF/JPG/PNG)					  ##
	// ##																  ##
	// #####################################################################
	
	public static void expImgByPdfBox(byte[] decodedBytes, String fileNamePre, String ext, boolean isBW, int height, int width) {   
		try {
	    	final PDDocument document = PDDocument.load(decodedBytes);
            	PDFRenderer pdfRenderer = new PDFRenderer(document);
            	for (int page = 0; page < document.getNumberOfPages(); ++page){
            		String pageNo = String.valueOf(page);
            		String fileName = fileNamePre;
            		BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 200);
                	if (document.getNumberOfPages() > 1) {
                		fileName = fileNamePre + pageNo;
                	}
                	expImg(bim, fileName, ext, isBW, height, width);
            	}
            	document.close();
        	} catch (IOException e){
            	e.printStackTrace();
        }
	}
	
	public static void expImgByPdfBox(byte[] decodedBytes, String fileNamePre, String ext, boolean isBW) {
		expImgByPdfBox(decodedBytes, fileNamePre, ext, isBW, 0, 0);
	}
	
	public static void expImg(BufferedImage bim, String fileName, String ext, boolean isBW, int height, int width) {
		if (height > 0 && width > 0) {
			bim = resizeImage(bim, height, width);
		}
    	try {
    		if(ext == ".tif"){
    			expTifImg(bim, fileName, isBW);
    		}
    		else {
    			expAnyImg(bim , fileName, ext, isBW);
    		}
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
	}
	
	public static void expImg(BufferedImage bim, String fileName, String ext, boolean isBW) {
		expImg(bim, fileName, ext, isBW, 0, 0);
	}
	
	public static void expTifImg(BufferedImage image, String fileName, boolean isBW) {
		   try {

		   if(image.getColorModel().getPixelSize() != 1 && isBW) {
			   image = imgChBW(image);				   
		   }

 		   OutputStream out = new FileOutputStream(fileName + ".tif");
 		   TIFFEncodeParam param = new TIFFEncodeParam();
 		   if(isBW) {
 			  param.setCompression(TIFFEncodeParam.COMPRESSION_GROUP4);
 		   }
 		   else {
 			   param.setCompression(TIFFEncodeParam.COMPRESSION_NONE);
 		   }
 		   param.setDeflateLevel(1);
 		   TIFFImageEncoder encoder = new TIFFImageEncoder(out, param);
 		   encoder.encode(image);
 		   out.close();
		   } catch (Exception e) {
		       e.printStackTrace();
		   }
	}
	
	// #####################################################################
	// ##																  ##
	// ##							Image Decode						  ##
	// ##																  ##
	// #####################################################################
	
	public static byte[] imgDecode(String encodedBytes) {
		byte[] decodedBytes = Base64.getDecoder().decode(encodedBytes.getBytes());
		return decodedBytes;
	}
	
	/* PDF Only*/
	public static BufferedImage strToImage(String encodedBytes, String ext) {
		try {			
			byte[] decodedBytes = imgDecode(encodedBytes);
			
			return strToImage(decodedBytes, ext);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/* PDF Only*/
	public static BufferedImage strToImage(byte[] decodedBytes, String ext) {
		try {			
			ByteArrayInputStream bis = new ByteArrayInputStream(decodedBytes);
			Iterator<?> readers = ImageIO.getImageReadersByFormatName(ext.substring(1));
			
			ImageReader reader = (ImageReader) readers.next();
			Object source = bis; 
			ImageInputStream iis = ImageIO.createImageInputStream(source); 
			reader.setInput(iis, true);
			ImageReadParam param = reader.getDefaultReadParam();
			
			Image image = reader.read(0, param);
			
			BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
			return bufferedImage;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// #####################################################################
	// ##																  ##
	// ##							Image Encode						  ##
	// ##																  ##
	// #####################################################################
	
	public static String encodeImg(byte[] src) {
		   byte[] encodedBytes = Base64.getEncoder().encode(src);
		   String encodedStr = new String(encodedBytes);
		   return encodedStr;
	}
	
	public static String encodeImg(ByteBuffer src) {
		   ByteBuffer encodedBytes = Base64.getEncoder().encode(src);
		   String encodedStr = new String(encodedBytes.array());
		   return encodedStr;
	}
	
	public static String encodeImgFromPath(String path) {
		String encodedStr = "";
		try {
			byte[] input_file = Files.readAllBytes(Paths.get(path));
			byte[] encodedBytes = Base64.getEncoder().encode(input_file);
			encodedStr = new String(encodedBytes);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return encodedStr;
	}
	
	// #####################################################################
	// ##																  ##
	// ##							Export Image						  ##
	// ##																  ##
	// #####################################################################
	
	public static void expImg(byte[] decodedBytes, String path) {
		try (OutputStream stream = new FileOutputStream(path)) {
		    stream.write(decodedBytes);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void expAnyImg(BufferedImage bim, String fileName, String ext, boolean isBW) {
		try {
			if(bim.getColorModel().getPixelSize() != 1 && isBW) {
				bim = imgChBW(bim);				   
		   }
			ImageIO.write(bim , ext.substring(1), new File(fileName + ext));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// #####################################################################
	// ##																  ##
	// ##							Miscellaneous						  ##
	// ##																  ##
	// #####################################################################
	
	private static BufferedImage resizeImage(BufferedImage originalImage, int height, int width){
		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();
			
		return resizedImage;
	}
	
	public static BufferedImage imgChBW(BufferedImage src) {
		IndexColorModel icm = new IndexColorModel(1, 2, new byte[] { (byte) 0, (byte) 0xFF }, new byte[] { (byte) 0, (byte) 0xFF }, new byte[] { (byte) 0, (byte) 0xFF });
		BufferedImage bufImg = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_BINARY, icm);

		ColorConvertOp cco = new ColorConvertOp(src.getColorModel().getColorSpace(), bufImg.getColorModel().getColorSpace(), null);
		cco.filter(src, bufImg);

		return bufImg;
	}
	
	public static BufferedImage getBufImg(byte[] imgByte) {
		try {					
			InputStream in = new ByteArrayInputStream(imgByte);
			BufferedImage bImageFromConvert = ImageIO.read(in);
			return bImageFromConvert;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] getBArr(BufferedImage bim, String ext) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] imageInByte = "".getBytes();
		try {
			ImageIO.write(bim, ext.substring(1), baos);
			baos.flush();
			imageInByte = baos.toByteArray();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imageInByte;
	}
	
	public static byte[] getBArr(BufferedImage bim) {
		return getBArr(bim, ".tif");
	}
}

