package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto;

public class SignedContentDTO {

	private byte[] content;
	private boolean flagSigned;
	private Exception e;
	private String fileName;
	
	public SignedContentDTO(Exception inE) {
		e = inE;
		flagSigned = false;
		content = null;
		fileName = null;
	}
	
	public SignedContentDTO(String inFileName, byte[] inContent) {
		e = null;
		flagSigned = true;
		content = inContent;
		fileName = inFileName;
	}

	public byte[] getContent() {
		return content;
	}

	public boolean isFlagSigned() {
		return flagSigned;
	}

	public Exception getE() {
		return e;
	}

	public String getFileName() {
		return fileName;
	}
	
}
