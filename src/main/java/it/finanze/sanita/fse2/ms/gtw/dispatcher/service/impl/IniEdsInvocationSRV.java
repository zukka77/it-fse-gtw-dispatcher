package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
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
	public Boolean insert(final String workflowInstanceId, final ResourceDTO fhirResourceDTO, final JWTTokenDTO jwtToken) {
		Boolean output = false;
		try {
			IniEdsInvocationETY etyToSave = buildETY(workflowInstanceId, fhirResourceDTO.getBundleJson(), fhirResourceDTO.getSubmissionSetEntryJson(),
					fhirResourceDTO.getDocumentEntryJson(), StringUtility.toJSON(jwtToken), null);
			etyToSave = iniInvocationRepo.insert(etyToSave);
			output = !StringUtility.isNullOrEmpty(etyToSave.getId());
		} catch(Exception ex) {
			log.error("Error while insert ini invocation item : " , ex);
			throw new BusinessException("Error while insert ini invocation item : " , ex);
		}
		return output; 
	}
	
	private IniEdsInvocationETY buildETY(final String workflowInstanceId, final String bundleJson, final String submissionSetEntryJson,
			final String documentEntryJson, final String tokenEntryJson, final String identificativoDocumento) {
		IniEdsInvocationETY out = new IniEdsInvocationETY();
		out.setWorkflowInstanceId(workflowInstanceId);
		out.setData(Document.parse(bundleJson));

		if (!StringUtility.isNullOrEmpty(identificativoDocumento)) {
			out.setIdDoc(identificativoDocumento);
		}
		
		List<Document> metadata = new ArrayList<>();
		Document submissionSetEntryDoc = new Document("submissionSetEntry" ,Document.parse(submissionSetEntryJson));
		Document documentEntryDoc = new Document("documentEntry" ,Document.parse(documentEntryJson));
		Document tokenEntry = new Document("tokenEntry", Document.parse(tokenEntryJson));
		
		metadata.add(submissionSetEntryDoc);
		metadata.add(documentEntryDoc);
		metadata.add(tokenEntry);
		
		out.setMetadata(metadata);
		return out;
	}

	@Override
	public Boolean replace(String workflowInstanceId, ResourceDTO fhirResourceDTO, JWTTokenDTO jwtToken, final String identificativoDocumento) {
		Boolean output = false;
		try {
			IniEdsInvocationETY etyToSave = buildETY(workflowInstanceId, fhirResourceDTO.getBundleJson(), fhirResourceDTO.getSubmissionSetEntryJson(),
					fhirResourceDTO.getDocumentEntryJson(), StringUtility.toJSON(jwtToken), identificativoDocumento);
			etyToSave = iniInvocationRepo.insert(etyToSave);
			output = !StringUtility.isNullOrEmpty(etyToSave.getId());
		} catch(Exception ex) {
			log.error("Error in replace while insert ini invocation item : " , ex);
			throw new BusinessException("Error in replace while insert ini invocation item : " , ex);
		}
		return output; 
	}

}
