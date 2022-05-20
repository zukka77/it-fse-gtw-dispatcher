package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.entity.IniEdsInvocationETY;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo.IIniEdsInvocationRepo;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IIniEdsInvocationSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IniEdsInvocationSRV implements IIniEdsInvocationSRV {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -8304667907412125924L;

	@Autowired
	private IIniEdsInvocationRepo iniInvocationRepo;
	
	@Override
	public Boolean insert(final String transactionId, final FhirResourceDTO fhirResourceDTO) {
		Boolean output = false;
		try {
			IniEdsInvocationETY etyToSave = buildETY(transactionId, fhirResourceDTO.getDocumentReferenceJson(),fhirResourceDTO.getSubmissionSetEntryJson(),
					fhirResourceDTO.getDocumentEntryJson());
			etyToSave = iniInvocationRepo.insert(etyToSave);
			output = !StringUtility.isNullOrEmpty(etyToSave.getId());
		} catch(Exception ex) {
			log.error("Error while insert ini invocation item : " , ex);
			throw new BusinessException("Error while insert ini invocation item : " , ex);
		}
		return output; 
	}
	
	private IniEdsInvocationETY buildETY(final String transactionId, final String documentReference, final String submissionSetEntryJson,
			final String documentEntryJson) {
		IniEdsInvocationETY out = new IniEdsInvocationETY();
		out.setTransactionId(transactionId);
		out.setData(Document.parse(documentReference));
		
		List<Document> metadata = new ArrayList<>();
		Document submissionSetEntryDoc = new Document("submissionSetEntry" ,Document.parse(submissionSetEntryJson));
		Document documentEntryDoc = new Document("documentEntry" ,Document.parse(documentEntryJson));
		
		metadata.add(submissionSetEntryDoc);
		metadata.add(documentEntryDoc);
		
		out.setMetadata(metadata);
		return out;
	}

}
