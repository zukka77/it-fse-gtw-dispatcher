package it.finanze.sanita.fse2.ms.gtw.dispatcher.utility;

import com.lowagie.text.pdf.*;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.AttachmentDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PDFUtility {
	
	public static Map<String, AttachmentDTO> extractAttachments(byte[] bytePDF) {
		Map<String, AttachmentDTO> out = new HashMap<>();
		
		try (PDDocument pd = PDDocument.load(bytePDF)) {
		    PDDocumentCatalog catalog = pd.getDocumentCatalog();
		    PDDocumentNameDictionary names = catalog.getNames();
		    PDEmbeddedFilesNameTreeNode embeddedFiles = names.getEmbeddedFiles();
		    Map<String, PDComplexFileSpecification> embeddedFileNames = embeddedFiles.getNames();
		    for (Map.Entry<String, PDComplexFileSpecification> entry : embeddedFileNames.entrySet()) {  
		    	AttachmentDTO att = AttachmentDTO.builder().
		    			fileName(entry.getKey()).
		    			mimeType(entry.getValue().getEmbeddedFile().getSubtype()).
		    			content(entry.getValue().getEmbeddedFile().toByteArray()).
		    			build();
		    	out.put(entry.getKey(), att);
		    }
		} catch (Exception e) {
			log.warn("Errore in fase di estrazione allegati da pdf.");
		}
	    return out;
	}
	
	public static String unenvelopeA2(byte[] pdf) {
		String out = null;
		String errorMsg = "No CDA found.";
		try (PdfReader pdfReader = new PdfReader(pdf)) {
			PdfDictionary catalog = pdfReader.getCatalog();
			if (catalog != null) {
				PdfDictionary names = catalog.getAsDict(PdfName.NAMES);
				if (names != null) {
					PdfIndirectReference xfaResourcesIndirect = names.getAsIndirectObject(new PdfName("XFAResources"));
					if (xfaResourcesIndirect != null) {
						PdfDictionary xfaResourcesDictionary = (PdfDictionary) pdfReader.getPdfObject(xfaResourcesIndirect.getNumber());
						if (xfaResourcesDictionary != null) {
							PdfArray pdfArrayDataset = (PdfArray) xfaResourcesDictionary.get(PdfName.NAMES);
							if (pdfArrayDataset != null) {
								if (pdfArrayDataset.size() >= 2) {
									PdfString pdfStringDataset = (PdfString) pdfArrayDataset.getPdfObject(0);
									if (pdfStringDataset != null) {
										String datasets = new String(pdfStringDataset.getBytes());
										if ("datasets".equals(datasets)) {
											PdfIndirectReference cdaIndirect = (PdfIndirectReference) pdfArrayDataset.getPdfObject(1);
											if (cdaIndirect != null) {
												PRStream cdaPRStream = (PRStream) pdfReader.getPdfObject(cdaIndirect.getNumber());
												if (cdaPRStream != null) {
													out = new String(PdfReader.getStreamBytes(cdaPRStream));
												} else {
													errorMsg = "PRStream cdaPRStream [element 1] is null";
												}
											} else {
												errorMsg = "PdfIndirectReference cdaIndirect [element 1] is null";
											}
										} else {
											errorMsg = "PdfString pdfStringDataset [element 0] String content is not equals datasets";
										}
									} else {
										errorMsg = "PdfString pdfStringDataset [element 0] is null";
									}
								} else {
									errorMsg = "PdfArray pdfArrayDataset has less than 2 elements";
								}
							} else {
								errorMsg = "PdfArray pdfArrayDataset is null";
							}
						} else {
							errorMsg = "PdfDictionary xfaResourcesDictionary is null";
						}
					} else {
						errorMsg = "PdfIndirectReference xfaResourcesIndirect inside Names is null";
					}
				} else {
					errorMsg = "PdfDictionary names inside catalog is null";
				}
			} else {
				errorMsg = "PdfDictionary catalog is null";
			}
		} catch (Exception e) {
			log.warn("Errore in fase di recupero CDA da risorsa.", e);
		} finally {
			log.warn(errorMsg);
		}
		return out;
	}

	public static boolean isPdf(byte[] pdf) {
		boolean out = false;
		if (pdf!=null && pdf.length >4) {
			byte[] magicNumber = Arrays.copyOf(pdf, 4);
			String strMagicNumber = new String(magicNumber);
			out = strMagicNumber.equals("%PDF");
		}
		return out;
	}

}
