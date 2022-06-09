//package it.finanze.sanita.fse2.ms.gtw.dispatcher;
//
//import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import org.apache.kafka.clients.producer.RecordMetadata;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
//import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
//import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
//import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
//import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.PublicationResultEnum;
//import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ValidationResultEnum;
//import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
//import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.EncryptDecryptUtility;
//import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "kafka.topic=test")
//@ActiveProfiles(Constants.Profile.TEST)
//public class KafkaTest {
//	
//	@Autowired
//	private IKafkaSRV kafkaSRV;
//
//	@Test
//	@DisplayName("Producer send")
//	void kafkaProducerSend() {  
//		String key = "1";
//		String topic = "transactionEvents"; // kafkaCFG.getTopic();
// 
//		/*****************TOPIC**********************/ 
//		for(int i=0; i<10; i++) { 
//			KafkaMessageDTO msg = new KafkaMessageDTO();
//			msg.setMessage("Messaggio numero : " + 1);
//			String message = EncryptDecryptUtility.encryptObject("fse", msg);
//			RecordMetadata output = kafkaSRV.sendMessage(topic,key, message, true);
//			assertEquals(message.length(), output.serializedValueSize() , "Il value non coincide");
//			assertEquals(topic,output.topic(), "Il topic non coincide");
//		} 
//		 
//    }
//
//
//	@Test
//	@DisplayName("Send transaction events")
//	void t1() {
//
//		String transactionID = StringUtility.generateUUID();
//
//		// --- send validation event
//		ValidationCDAReqDTO validationReq = ValidationCDAReqDTO.builder().
//				activity(ActivityEnum.VALIDATION).
//				identificativoDoc(StringUtility.generateUUID()).
//				identificativoPaziente(StringUtility.generateUUID()).
//				identificativoSottomissione(StringUtility.generateUUID()).build();
//
//		ValidationResultEnum validationResult = ValidationResultEnum.SYNTAX_ERROR;
//
//		assertDoesNotThrow(() -> kafkaSRV.notifyValidationEvent(validationReq, validationResult, false, false, transactionID));
//
//		// --- send publication event
//
//		PublicationCreationReqDTO publicationReq = PublicationCreationReqDTO.builder().transactionID(transactionID)
//				.identificativoDoc(StringUtility.generateUUID())
//				.identificativoPaziente(StringUtility.generateUUID())
//				.identificativoSottomissione(StringUtility.generateUUID()).forcePublish(false).build();
//
//		PublicationResultEnum publicationResult = PublicationResultEnum.PUBLISHING_ERROR;
//
//		assertDoesNotThrow(() -> kafkaSRV.notifyPublicationEvent(publicationReq, publicationResult, false, false));
//
//		// --- send historical document events (validation + publication)
//
//		String historicalTransactionID = StringUtility.generateUUID();
//		ValidationCDAReqDTO historicalValidationReq = ValidationCDAReqDTO.builder().
//				activity(ActivityEnum.HISTORICAL_DOC_PRE_PUBLISHING).
//				identificativoDoc(StringUtility.generateUUID()).
//				identificativoPaziente(StringUtility.generateUUID()).
//				identificativoSottomissione(StringUtility.generateUUID()).build();
//
//		ValidationResultEnum historicalValidationResult = ValidationResultEnum.OK;
//
//		assertDoesNotThrow(() -> kafkaSRV.notifyValidationEvent(historicalValidationReq, historicalValidationResult, true, false, transactionID));
//
//		// publication
//
//		PublicationCreationReqDTO historicalDocReq = PublicationCreationReqDTO.builder().transactionID(historicalTransactionID)
//				.identificativoDoc(StringUtility.generateUUID())
//				.identificativoPaziente(StringUtility.generateUUID())
//				.identificativoSottomissione(StringUtility.generateUUID()).forcePublish(false).build();
//
//		PublicationResultEnum historicalDocResult = PublicationResultEnum.OK;
//
//		assertDoesNotThrow(() -> kafkaSRV.notifyPublicationEvent(historicalDocReq, historicalDocResult, true, false));
//
//	}
//	
//	@Data
//	@NoArgsConstructor
//	class KafkaMessageDTO{
//		String message;
//		
//	}
//}
