/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.Getter;

public enum EventCodeEnum {

	P99("P99", "Oscuramento del documento"),
	P97("P97", "Oscuramento al genitore"),
	P98("P98", "Oscuramento all’assistito"),
	J07BX03("J07BX03", "Vaccino per Covid-19"),
	LP418019_8("LP418019-8", "Tampone antigenico per Covid-19"),
	LP417541_2("LP417541-2", "Tampone molecolare per Covid-19"),
	_96118_5("96118-5", "Test Sierologico qualitativo"),
	_94503_0("94503-0", "Test Sierologico quantitativo"),
	pay("pay", "Prescrizione farmaceutica non a carico SSN"),
	PUBLICPOL("PUBLICPOL", "Prescrizione farmaceutica SSN"),
	LP267463_0("LP267463-0", "Reddito"),
	LP199190_2("LP199190-2", "Patologia"),
	_90768_3("90768-3", "Analisi sangue donatore"),
	J07AC("J07AC","Anthrax vaccines"),
	J07AC01("J07AC01","anthrax antigen"),
	J07AD("J07AD","Brucellosis vaccines"),
	J07AD01("J07AD01","brucella antigen"),
	J07AE("J07AE","Cholera vaccines"),
	J07AE01("J07AE01","cholera, inactivated, whole cell"),
	J07AE02("J07AE02","cholera, live attenuated"),
	J07AE51("J07AE51","cholera, combinations with typhoid vaccine, inactivated, whole cell"),
	J07AF("J07AF","Diphtheria vaccines"),
	J07AF01("J07AF01","diphtheria toxoid"),
	J07AG("J07AG","Haemophilus influenzae B vaccines"),
	J07AG01("J07AG01","haemophilus influenzae B, purified antigen conjugated"),
	J07AG51("J07AG51","haemophilus influenzae B, combinations with toxoids"),
	J07AG52("J07AG52","haemophilus influenzae B, combinations with pertussis and toxoids"),
	J07AG53("J07AG53","haemophilus influenzae B, combinations with meningococcus C, conjugated"),
	J07AG54("J07AG54","haemophilus influenza B, combinations with meningococcus C,Y, conjugated"),
	J07AH("J07AH","Meningococcal vaccines"),
	J07AH01("J07AH01","meningococcus A, purified polysaccharides antigen"),
	J07AH02("J07AH02","other meningococcal monovalent purified polysaccharides antigen"),
	J07AH03("J07AH03","meningococcus A,C, bivalent purified polysaccharides antigen"),
	J07AH04("J07AH04","meningococcus A,C,Y,W-135, tetravalent purified polysaccharides antigen"),
	J07AH05("J07AH05","other meningococcal polyvalent purified polysaccharides antigen"),
	J07AH06("J07AH06","meningococcus B, outer membrane vesicle vaccine"),
	J07AH07("J07AH07","meningococcus C, purified polysaccharides antigen conjugated"),
	J07AH08("J07AH08","meningococcus A,C,Y,W-135, tetravalent purified polysaccharides antigen conjugated"),
	J07AH09("J07AH09","meningococcus B, multicomponent vaccine"),
	J07AH10("J07AH10","meningococcus A, purified polysaccharides antigen conjugated"),
	J07AJ("J07AJ","Pertussis vaccines"),
	J07AJ01("J07AJ01","pertussis, inactivated, whole cell"),
	J07AJ02("J07AJ02","pertussis, purified antigen"),
	J07AJ51("J07AJ51","pertussis, inactivated, whole cell, combinations with toxoids"),
	J07AJ52("J07AJ52","pertussis, purified antigen, combinations with toxoids"),
	J07AK("J07AK","Plague vaccines"),
	J07AK01("J07AK01","plague, inactivated, whole cell"),
	J07AL("J07AL","Pneumococcal vaccines"),
	J07AL01("J07AL01","pneumococcus, purified polysaccharides antigen"),
	J07AL02("J07AL02","pneumococcus, purified polysaccharides antigen conjugated"),
	J07AL52("J07AL52","pneumococcus purified polysaccharides antigen and haemophilus influenzae, conjugated"),
	J07AM("J07AM","Tetanus vaccines"),
	J07AM01("J07AM01","tetanus toxoid"),
	J07AM51("J07AM51","tetanus toxoid, combinations with diphtheria toxoid"),
	J07AM52("J07AM52","tetanus toxoid, combinations with tetanus immunoglobulin"),
	J07AN("J07AN","Tuberculosis vaccines"),
	J07AN01("J07AN01","tuberculosis, live attenuated"),
	J07AP("J07AP","Typhoid vaccines"),
	J07AP01("J07AP01","typhoid, oral, live attenuated"),
	J07AP02("J07AP02","typhoid, inactivated, whole cell"),
	J07AP03("J07AP03","typhoid, purified polysaccharide antigen"),
	J07AP10("J07AP10","typhoid, combinations with paratyphi types"),
	J07AR("J07AR","Typhus (exanthematicus) vaccines"),
	J07AR01("J07AR01","typhus exanthematicus, inactivated, whole cell"),
	J07AX("J07AX","Other bacterial vaccines"),
	J07AX01("J07AX01","leptospira vaccines"),
	J07BA("J07BA","Encephalitis vaccines"),
	J07BA01("J07BA01","encephalitis, tick borne, inactivated, whole virus"),
	J07BA02("J07BA02","encephalitis, Japanese, inactivated, whole virus"),
	J07BA03("J07BA03","encephalitis, Japanese, live attenuated"),
	J07BB("J07BB","Influenza vaccines"),
	J07BB01("J07BB01","influenza, inactivated, whole virus"),
	J07BB02("J07BB02","influenza, inactivated, split virus or surface antigen"),
	J07BB03("J07BB03","influenza, live attenuated"),
	J07BB04("J07BB04","influenza, virus like particles"),
	J07BC("J07BC","Hepatitis vaccines"),
	J07BC01("J07BC01","hepatitis B, purified antigen"),
	J07BC02("J07BC02","hepatitis A, inactivated, whole virus"),
	J07BC20("J07BC20","combinations"),
	J07BD("J07BD","Measles vaccines"),
	J07BD01("J07BD01","measles, live attenuated"),
	J07BD51("J07BD51","measles, combinations with mumps, live attenuated"),
	J07BD52("J07BD52","measles, combinations with mumps and rubella, live attenuated"),
	J07BD53("J07BD53","measles, combinations with rubella, live attenuated"),
	J07BD54("J07BD54","measles, combinations with mumps, rubella and varicella, live attenuated"),
	J07BE("J07BE","Mumps vaccines"),
	J07BE01("J07BE01","mumps, live attenuated"),
	J07BF("J07BF","Poliomyelitis vaccines"),
	J07BF01("J07BF01","poliomyelitis oral, monovalent, live attenuated"),
	J07BF02("J07BF02","poliomyelitis oral, trivalent, live attenuated"),
	J07BF03("J07BF03","poliomyelitis, trivalent, inactivated, whole virus"),
	J07BF04("J07BF04","poliomyelitis oral, bivalent, live attenuated"),
	J07BG("J07BG","Rabies vaccines"),
	J07BG01("J07BG01","rabies, inactivated, whole virus"),
	J07BH("J07BH","Rota virus diarrhea vaccines"),
	J07BH01("J07BH01","rota virus, live attenuated"),
	J07BH02("J07BH02","rota virus, pentavalent, live, reassorted"),
	J07BJ("J07BJ","Rubella vaccines"),
	J07BJ01("J07BJ01","rubella, live attenuated"),
	J07BJ51("J07BJ51","rubella, combinations with mumps, live attenuated"),
	J07BK("J07BK","Varicella zoster vaccines"),
	J07BK01("J07BK01","varicella, live attenuated"),
	J07BK02("J07BK02","zoster, live attenuated"),
	J07BK03("J07BK03","zoster, purified antigen"),
	J07BL("J07BL","Yellow fever vaccines"),
	J07BL01("J07BL01","yellow fever, live attenuated"),
	J07BM("J07BM","Papillomavirus vaccines"),
	J07BM01("J07BM01","papillomavirus (human types 6, 11, 16, 18)"),
	J07BM02("J07BM02","papillomavirus (human types 16, 18)"),
	J07BM03("J07BM03","papillomavirus (human types 6, 11, 16, 18, 31, 33, 45, 52, 58)"),
	J07BN("J07BN","Covid-19 vaccines"),
	J07BN01("J07BN01","covid-19, RNA-based vaccine"),
	J07BN02("J07BN02","covid-19, viral vector, non-replicating"),
	J07BN03("J07BN03","covid-19, inactivated virus"),
	J07BN04("J07BN04","covid-19, protein subunit"),
	J07BX("J07BX","Other viral vaccines"),
	J07BX01("J07BX01","smallpox vaccines"),
	J07BX02("J07BX02","ebola vaccines"),
	J07BX04("J07BX04","dengue virus vaccines"),
	J07CA("J07CA","Bacterial and viral vaccines, combined"),
	J07CA01("J07CA01","diphtheria-poliomyelitis-tetanus"),
	J07CA02("J07CA02","diphtheria-pertussis-poliomyelitis-tetanus"),
	J07CA03("J07CA03","diphtheria-rubella-tetanus"),
	J07CA04("J07CA04","haemophilus influenzae B and poliomyelitis"),
	J07CA05("J07CA05","diphtheria-hepatitis B-pertussis-tetanus"),
	J07CA06("J07CA06","diphtheria-haemophilus influenzae B-pertussis-poliomyelitis-tetanus"),
	J07CA07("J07CA07","diphtheria-hepatitis B-tetanus"),
	J07CA08("J07CA08","haemophilus influenzae B and hepatitis B"),
	J07CA09("J07CA09","diphtheria-haemophilus influenzae B-pertussis-poliomyelitis-tetanus-hepatitis B"),
	J07CA10("J07CA10","typhoid-hepatitis A"),
	J07CA11("J07CA11","diphtheria-haemophilus influenzae B-pertussis-tetanus-hepatitis B"),
	J07CA12("J07CA12","diphtheria-pertussis-poliomyelitis-tetanus-hepatitis B"),
	J07CA13("J07CA13","diphtheria-haemophilus influenzae B-pertussis-tetanus-hepatitis B-meningococcus A + C"),
	J07XA("J07XA","Parasitic vaccines"),
	J07XA01("J07XA01","malaria vaccines");
	@Getter
	private String code;

	@Getter
	private String description;

	private EventCodeEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

	public static EventCodeEnum fromValue(final String code) {
		EventCodeEnum output = null;
        for (EventCodeEnum valueEnum : EventCodeEnum.values()) {
        	if (valueEnum.getCode().equalsIgnoreCase(code)) {
        		output = valueEnum;
        		break;
        	}

        }
		 
		return output;
    }
}
