package it.finanze.sanita.fse2.ms.gtw.dispatcher.client;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.client.DocumentReferenceResDTO;

public interface IFhirMappingClient extends Serializable {

	DocumentReferenceResDTO callConvertCdaInBundle(final FhirResourceDTO resourceToConvert);
}
