package org.swasth.hcx.fhirexamples;

import org.hl7.fhir.r4.model.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class OnActionFhirExamples {

    public static CoverageEligibilityResponse coverageEligibilityResponseExample() {
        //Creating the coverage eligibility response
        CoverageEligibilityResponse covelires = new CoverageEligibilityResponse();
        covelires.setId(UUID.randomUUID().toString());
        Meta metaResponse = new Meta();
        metaResponse.getProfile().add(new CanonicalType("https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-CoverageEligibilityResponse.html"));
        metaResponse.setLastUpdated(new Date());
        covelires.setMeta(metaResponse);
        covelires.addIdentifier(new Identifier().setSystem("http://identifiersystem.com").setValue("IdentifierValue"));
        covelires.setStatus(CoverageEligibilityResponse.EligibilityResponseStatus.ACTIVE);
        covelires.setPatient(new Reference("Patient/RVH1003"));
        covelires.setCreated(new Date());
        covelires.setInsurer(new Reference("Organization/GICOFINDIA"));
        covelires.setRequest(new Reference("CoverageEligibilityRequest/dc82673b-8c71-48c2-8a17-16dcb3b035f6"));
        covelires.setRequestor(new Reference("Organization/WeMeanWell01"));
        covelires.setOutcome(Enumerations.RemittanceOutcome.COMPLETE);
        EnumFactory<CoverageEligibilityResponse.EligibilityResponsePurpose> fact = new CoverageEligibilityResponse.EligibilityResponsePurposeEnumFactory();
        covelires.setPurpose(List.of((Enumeration) new Enumeration<>(fact).setValue(CoverageEligibilityResponse.EligibilityResponsePurpose.BENEFITS)));
        return covelires;
    }

    public static ClaimResponse claimResponseExample(){
        //Creating Claim response
        ClaimResponse claimRes = new ClaimResponse();
        claimRes.setId(UUID.randomUUID().toString());
        Meta metaClaimRes = new Meta();
        metaClaimRes.getProfile().add(new CanonicalType("https://ig.hcxprotocol.io/v0.7.1/StructureDefinition-ClaimResponse.html"));
        metaClaimRes.setLastUpdated(new Date());
        claimRes.setMeta(metaClaimRes);
        claimRes.setStatus(ClaimResponse.ClaimResponseStatus.ACTIVE);
        claimRes.addIdentifier(new Identifier().setSystem("http://identifiersystem.com").setValue(UUID.randomUUID().toString()));
        claimRes.setType(new CodeableConcept(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/claim-type").setCode("institutional")));
        claimRes.setUse(ClaimResponse.Use.CLAIM);
        claimRes.setPatient(new Reference("Patient/RVH1003"));
        claimRes.setCreated(new Date());
        claimRes.setInsurer(new Reference("Organization/GICOFINDIA"));
        claimRes.setRequestor(new Reference("Organization/WeMeanWell01"));
        claimRes.setRequest(new Reference("Claim/CLAIM1"));
        claimRes.setOutcome(ClaimResponse.RemittanceOutcome.COMPLETE);
        claimRes.getTotal().add(new ClaimResponse.TotalComponent().setCategory(new CodeableConcept(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/adjudication").setCode("benefit"))).setAmount(new Money().setValue(80000).setCurrency("INR")));
        //claimRes.getError().add(new ClaimResponse.ErrorComponent(new CodeableConcept(new Coding().setSystem("http://hcxprotocol.io/codes/claim-error-codes").setCode("AUTH-005").setDisplay("\tClaim information is inconsistent with pre-certified/authorized services"))));
        return claimRes;
    }

}
